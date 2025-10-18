# Plan – Démo interactive GL-Solver

## Objectif général
Créer deux démonstrations ludiques et indépendantes du dépôt académique officiel :
1. **Application JavaFX** « Planning de soirée entre amis » exploitant le solveur via sa logique d’affectation.
2. **Appli Web** (backend Java/Spring Boot ou Node.js + frontend léger) présentant un cas similaire côté navigateur.

> Ces projets resteront sur la branche locale `demo-app` (ou dans un dépôt privé) et ne seront pas poussés sur la branche `master`.

---

## 1. Préparation
- [x] Créer la branche locale `demo-app` et isoler le travail
- [x] Générer le jar du solver (`./gradlew jar`) pour réutilisation
- [x] Poser la structure de base :
  - dossier `demo/javafx-planning` (Gradle + JavaFX, jar attendu dans `libs/`)
  - dossier `demo/web-planning` (Gradle + Spring Boot, jar attendu dans `libs/`)
- [x] Définir les jeux de données (amis, activités, préférences, contraintes)

---

## 2. Démo JavaFX – Planning de soirée
1. **Structure projet**
   - [x] Créer module `demo/javafx-planning` avec JavaFX 21, dépendance jar solver dans `libs/`
   - [x] Ajouter un bridge solver complet (`SolverBridge.solveDemoProblem`)
2. **Interface utilisateur**
   - [ ] Remplacer l’UI mock par de vrais formulaires (table d’édition, curseurs, etc.)
   - [ ] Ajouter animations/visuels (avatars, icônes)
3. **Couche solveur**
   - [x] Convertir les préférences saisies → coûts/matrice 0/1
   - [x] Construire le problème (variables, contraintes all-diff, minimisation)
   - [x] Afficher plusieurs solutions (top 3) si besoin
4. **Expérience utilisateur**
   - [ ] Bouton « Voir une autre option » / export texte ou PDF (optionnel)
5. **Tests / packaging**
   - [x] Documenter les commandes (`./gradlew run`) + prérequis JavaFX

---

## 3. Démo Web – API + Interface
1. **Backend Spring Boot**
   - [x] Créer module `demo/web-planning` avec endpoint `/planifier`
   - [x] Implémenter `SolverService` avec le solver réel
2. **Frontend**
   - [ ] Concevoir une page web (formulaire préférences, bouton « Calculer », affichage résultats)
   - [ ] Ajouter visualisation (cartes, jauges, historique)
3. **Fonctionnalités bonus**
   - [ ] Comparaison multi-objectifs (coût vs satisfaction)
   - [ ] Visualisation du nombre de nœuds explorés
4. **Intégration solver**
   - [ ] Centraliser les mappings données → solver (service partagé)
   - [ ] Ecrire des tests unitaires sur le service/contrôleur

---

## 4. Communication & livrables
- [ ] Rédiger README dédiés (lancement, présentation) – JavaFX (en cours), Web (en cours)
- [ ] Ajouter captures d’écran / GIF de démonstration
- [ ] Prévoir un script `launch-demo.sh` ou Docker (optionnel)
- [ ] Documenter les emprunts supplémentaires (si ChatGPT/Cursor utilisés)

---

## 5. Suivi & rappels
- Toujours vérifier que `master` reste propre avant de synchroniser.
- Ne pas introduire de dépendances lourdes dans le dépôt académique principal.
- Maintenir des commits explicites sur `demo-app` pour suivre l’avancement.
