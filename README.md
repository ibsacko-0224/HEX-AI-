
# 💎 HEX-AI — CENTRAL-HEX

Assistant intelligent Android développé avec Kotlin, Jetpack Compose et Node.js.

## Fonctionnalités

- Assistant IA conversationnel
- Aide à la programmation
- Générateur d'idées
- Traduction
- Rédaction et correction
- Interface noire et blanche
- Backend Node.js
- Protection de la clé API côté serveur
- Compilation APK via GitHub Actions

## Technologies

- Kotlin
- Android Jetpack Compose
- Node.js 20+
- Express
- OpenRouter API
- GitHub Actions

## Installation du backend

1. Ouvrir le dossier backend.
2. Installer les dépendances avec `npm install`.
3. Configurer les variables d'environnement :
   - OPENROUTER_API_KEY
   - OPENROUTER_MODEL
   - PORT
4. Lancer avec `npm start`.

Ne jamais publier la véritable clé API dans le dépôt GitHub.

## Connexion Android

Dans HEX-AI :
1. Ouvrir Réglages.
2. Entrer l'URL HTTPS du backend.
3. Enregistrer.
4. Envoyer un message.

Exemple :
https://mon-serveur.example.com

## Compilation APK

GitHub Actions compile l'application Android automatiquement.

Pour récupérer l'APK :
1. Ouvrir Actions.
2. Sélectionner HEX-AI Android APK.
3. Ouvrir la dernière exécution réussie.
4. Télécharger HEX-AI-APK.

Une Release GitHub est également créée automatiquement.

## Sécurité

La clé OpenRouter doit être stockée dans les variables secrètes
de l'hébergeur du backend.

Le backend doit être protégé contre les abus avant une ouverture
publique. Cette première version utilise une limitation de requêtes
par adresse IP, mais ne possède pas encore de système de comptes
utilisateurs ou d'authentification.

## Version

1.0.0 — Première base fonctionnelle de HEX-AI.

CENTRAL-HEX — L'innovation commence par une idée.
