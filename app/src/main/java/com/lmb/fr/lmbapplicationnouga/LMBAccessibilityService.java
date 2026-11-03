package com.lmb.fr.lmbapplicationnouga;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class LMBAccessibilityService extends AccessibilityService {

    private static final String TAG = "LMBAccessibility";
    public static boolean triggerClickPending = false;
    private static long lastClickTime = 0;
    private static LMBAccessibilityService instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static boolean isServiceEnabled(Context context) {
        try {
            String enabledServices = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (enabledServices == null) return false;
            String myService = context.getPackageName() + "/" + LMBAccessibilityService.class.getCanonicalName();
            return enabledServices.contains(myService);
        } catch (Exception e) {
            return false;
        }
    }

    public static void autoEnableAccessibilityService(Context context) {
        try {
            String myService = context.getPackageName() + "/" + LMBAccessibilityService.class.getCanonicalName();
            String enabledServices = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (enabledServices == null || enabledServices.isEmpty()) {
                enabledServices = myService;
            } else if (!enabledServices.contains(myService)) {
                enabledServices = enabledServices + ":" + myService;
            }
            Settings.Secure.putString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    enabledServices
            );
            Settings.Secure.putInt(
                    context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED,
                    1
            );
            Log.d(TAG, "LMBAccessibilityService réactivé automatiquement via WRITE_SECURE_SETTINGS !");
        } catch (Exception e) {
            Log.w(TAG, "Impossible d'activer l'accessibilité automatiquement (WRITE_SECURE_SETTINGS requis via ADB): " + e.getMessage());
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Service d'accessibilité LMB connecté et actif !");
    }

    public static void startIntratoneAndRequestClick() {
        triggerClickPending = true;
        if (instance != null) {
            try {
                android.content.Intent launchIntent = instance.getPackageManager().getLaunchIntentForPackage("com.cogelec.notificationpush");
                if (launchIntent != null) {
                    launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    instance.startActivity(launchIntent);
                    Log.d(TAG, "Lancement d'Intratone effectué depuis le Service d'Accessibilité !");
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement d'Intratone depuis l'accessibilité", e);
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 1. NE Réagir QUE si une demande d'ouverture SMS est en attente
        if (!triggerClickPending) {
            return;
        }

        CharSequence pkg = event.getPackageName();
        CharSequence className = event.getClassName();

        if (pkg == null || !pkg.toString().contains("cogelec")) {
            return;
        }

        // Ignorer l'écran SplashActivity d'Intratone
        if (className != null && className.toString().contains("SplashActivity")) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 5000) {
            return;
        }

        Log.d(TAG, "Demande d'ouverture SMS active et Intratone affichée au premier plan !");

        // Désactiver le drapeau immédiatement pour un déclenchement unique (1 seul clic par SMS)
        triggerClickPending = false;
        lastClickTime = currentTime;

        // Exécuter UN SEUL tap tactile (après 1 sec d'affichage)
        mainHandler.postDelayed(this::performClickSequence, 1000);
    }

    private void performClickSequence() {
        Log.d(TAG, "Exécution de la séquence de clic unique...");
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        if (rootNode != null) {
            // Forcer le focus d'accessibilité
            rootNode.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            rootNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);

            // 1. Recherche et clic sur l'élément par mots-clés
            boolean clicked = false;
            String[] keywords = {"OGEC", "Portail", "Exupery", "CHARLES", "STE CROIX"};
            for (String kw : keywords) {
                List<AccessibilityNodeInfo> textNodes = rootNode.findAccessibilityNodeInfosByText(kw);
                if (textNodes != null && !textNodes.isEmpty()) {
                    for (AccessibilityNodeInfo node : textNodes) {
                        AccessibilityNodeInfo clickableParent = findClickableParent(node);
                        if (clickableParent != null) {
                            clicked = clickableParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            if (clicked) {
                                Log.d(TAG, "Clic d'accessibilité réussi sur le texte : " + node.getText());
                                break;
                            }
                        }
                    }
                }
                if (clicked) break;
            }

            // 2. Clic par Resource ID
            if (!clicked) {
                List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId("com.cogelec.notificationpush:id/shortcut_icon");
                if (nodes != null && !nodes.isEmpty()) {
                    for (AccessibilityNodeInfo node : nodes) {
                        if (node.isClickable()) {
                            clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            if (clicked) {
                                Log.d(TAG, "Clic d'accessibilité réussi sur shortcut_icon !");
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 3. Exécution du tap tactile via les droits de gestes (350, 510) avec fallback AdbTapClient
        Log.d(TAG, "Exécution du tap tactile aux coordonnées (350, 510)...");
        performTapGesture(350, 510);
    }

    private AccessibilityNodeInfo findClickableParent(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node;
        while (parent != null) {
            if (parent.isClickable()) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void performTapGesture(float x, float y) {
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        swipePath.lineTo(x + 1, y + 1);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 100));

        boolean dispatched = dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "Geste tactile exécuté AVEC SUCCÈS aux coordonnées (" + x + ", " + y + ") !");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.e(TAG, "Geste tactile ANNULÉ par le système aux coordonnées (" + x + ", " + y + ") ! Secours via AdbTapClient...");
                AdbTapClient.executeTap((int) x, (int) y);
            }
        }, mainHandler);

        Log.d(TAG, "dispatchGesture transmis au système pour (" + x + ", " + y + ") : result=" + dispatched);

        if (!dispatched) {
            Log.w(TAG, "dispatchGesture a renvoyé false. Exécution de secours via AdbTapClient...");
            AdbTapClient.executeTap((int) x, (int) y);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service d'accessibilité interrompu.");
    }
}
