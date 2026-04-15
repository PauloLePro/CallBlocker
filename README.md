# CallBlocker

Application Android (Java, minSdk 29) qui bloque automatiquement certains appels entrants de démarchage en France à partir de préfixes configurables.

## Fonctionnalités

- Service `CallScreeningService` pour filtrer les appels entrants.
- Demande du rôle Android `ROLE_CALL_SCREENING`.
- Liste de préfixes bloqués (activation/désactivation + suppression + ajout).
- Préfixes FR préchargés par défaut :
  - `0162`, `0163`, `0270`, `0271`, `0377`, `0378`, `0424`, `0425`, `0568`, `0569`, `0948`, `0949`
- Whitelist (numéros autorisés jamais bloqués).
- Journal local des appels bloqués (numéro reçu, préfixe matché, date/heure) via Room.
- Export / import JSON des préfixes + whitelist.
- Réinitialisation aux préfixes FR par défaut.
- UI en français, thème clair/sombre via `DayNight`.

## Structure

- `MainActivity` : écran principal
- `SettingsActivity` : export/import/réinitialisation
- `CallBlockerScreeningService` : logique de filtrage d'appels
- `PrefixRepository` : persistance préfixes + whitelist (SharedPreferences JSON)
- `NumberNormalizer` : normalisation des numéros FR
- `Room` : stockage du journal (`BlockedCallLogEntity`, `BlockedCallLogDao`, `AppDatabase`)
- Adapters RecyclerView : préfixes, whitelist, historique

## Compilation

### Android Studio

1. Ouvrir le dossier `CallBlocker` dans Android Studio.
2. Laisser la synchronisation Gradle se terminer.
3. Compiler avec **Build > Make Project** ou lancer `app` sur un appareil Android 10+.

### Ligne de commande

Pré-requis : SDK Android installé et configuré (`ANDROID_HOME` ou `local.properties`).

```bash
cd CallBlocker
./gradlew assembleDebug
```

APK debug attendu : `app/build/outputs/apk/debug/app-debug.apk`

## Installation

- Installer l'APK debug :

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Activation du rôle de filtrage

1. Ouvrir l'application.
2. Cliquer sur **Activer le filtrage d'appels**.
3. Accepter la demande Android pour définir l'app comme service de filtrage d'appels.
4. Vérifier l'indicateur : **Protection active**.

Si besoin, utiliser **Ouvrir les paramètres système** pour régler les apps par défaut.

## Test avec numéros simulés

- Ajouter un préfixe actif (ex. `0162`).
- Simuler ou appeler vers l'appareil avec un numéro commençant par :
  - `0162123456`
  - `+33162123456`
  - `0033162123456`
- Vérifier que l'appel est rejeté et qu'une entrée apparaît dans l'historique.
- Ajouter ce numéro dans la whitelist et retester : l'appel ne doit plus être bloqué.

## Limites

- Le filtrage dépend du support Android constructeur/opérateur.
- Certains appels masqués/spoofés peuvent contourner le filtrage.
- Certains appareils appliquent différemment `skipCallLog` / `skipNotification`.
- Le rôle call screening n'implique pas que l'app remplace entièrement le dialer.

## Pistes d'amélioration

- Recherche full-text et filtres dans l'historique.
- Backup/restauration chiffrés.
- Synchronisation cloud optionnelle.
- Import de listes communautaires signées.
- Tests instrumentés (service + UI).
- Écran statistiques (volumétrie par préfixe/période).
