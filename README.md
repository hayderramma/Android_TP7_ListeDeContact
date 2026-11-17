# Contacts V3 — TP Android (Fragments • RecyclerView • Filtrage • Google Maps)

Projet Android Studio complet en **Kotlin + interfaces XML**, évolution du TP V2.
Une seule `MainActivity` héberge trois fragments dynamiques dans un `FragmentContainerView`.

## 1. Prérequis

- Android Studio (Arctic Fox ou plus récent ; testé avec Gradle 8.7 / AGP 8.5.2 / Kotlin 1.9.24)
- JDK 17
- Android SDK avec la plateforme **android-35**
- Un projet Google Cloud avec **Maps SDK for Android** activé (facturation requise par Google)

## 2. Configuration de la clé API (important)

La clé **ne doit jamais être commitée**.

1. Copier `local.properties.example` vers `local.properties` (déjà présent localement pour la build, mais ignoré par Git).
2. Renseigner :
   ```properties
   sdk.dir=C:/Users/Vous/AppData/Local/Android/Sdk
   MAPS_API_KEY=VOTRE_CLE_GOOGLE_MAPS
   ```
3. Dans Google Cloud, restreindre la clé : type **Android**, nom de paquet `com.example.contactsv3`
   et empreinte SHA-1 de votre keystore de debug/release.
4. `app/build.gradle.kts` lit `MAPS_API_KEY` depuis `local.properties` et l'injecte dans
   `AndroidManifest.xml` via `manifestPlaceholders["MAPS_API_KEY"]`.
5. Vérifier avant de pousser :
   ```bash
   git check-ignore -v local.properties
   ```
   Si le projet compile sans clé, la carte affiche un message explicite au lieu de planter.

## 3. Compiler et tester

```bash
./gradlew assembleDebug      # ou gradlew.bat sous Windows
./gradlew test               # tests unitaires du filtrage et des coordonnées
./gradlew connectedAndroidTest  # tests instrumentés (optionnels)
```

Ou simplement ouvrir le dossier dans Android Studio et lancer l'application sur un émulateur/appareil.

## 4. Structure du projet

```
app/src/main/java/com/example/contactsv3/
├── MainActivity.kt              Activité unique + navigation + titre
├── Contact.kt                   Modèle (id, nom, prénom, téléphone, e-mail, photo, lat, lon)
├── ContactsViewModel.kt         Collection en mémoire (source de vérité partagée)
├── ContactFilter.kt             Filtrage nom / prénom / téléphone, insensible à la casse
├── Coordinates.kt               Validation GPS (±90 / ±180), testable
├── ContactAdapter.kt            RecyclerView.Adapter + ViewHolder + DiffUtil
├── ListeContactsFragment.kt     Recherche + RecyclerView + état vide
├── DetailViewModel.kt           Brouillon de modification (survit à la rotation)
├── DetailContactFragment.kt     Édition, validation, Appel, E-mail, Galerie, Caméra, Map
├── TakePictureContract.kt       ActivityResultContract avec FLAG_GRANT_WRITE_URI_PERMISSION
├── CarteContactFragment.kt      MapView, marqueur, centrage, retour
└── ImageLoader.kt               Chargement photo + image de repli
```

Ressources XML : `res/layout/` (5 écrans/lignes), `res/values/` (chaînes, couleurs, thème),
`res/drawable/` (icôns vectorielles), `res/xml/file_paths.xml` (FileProvider pour la caméra).

## 5. Choix de conception

| Point | Choix | Justification |
|---|---|---|
| Passage au détail | `Bundle` avec **l'identifiant** du contact | Un identifiant reste stable après filtrage ou suppression ; une position de ligne change et provoque des erreurs d'indice. |
| Mise à jour de la liste | **ViewModel partagé** au niveau de l'activité | Le modèle vit dans un seul endroit, survit à la rotation, et la liste s'actualise automatiquement via `LiveData` dès que le détail enregistre. Plus simple qu'un Fragment Result pour une source de données unique. |
| Brouillon du formulaire | ViewModel propre au fragment `Detail` | Les champs (même invalides) survivent à la rotation ; « Annuler » ne touche jamais le ViewModel partagé. |
| Carte | `MapView` avec delegation explicite du cycle de vie | Évite d'empiler des `SupportMapFragment` enfants lors des recréations. |
| Photos | `ActivityResultContracts.GetContent()` + contrat caméra maison | Gestion propre des annulations, permissions URI persistantes et `FileProvider`. |

## 6. Navigation

- `MainActivity.onCreate()` : `replace()` de la liste **uniquement si `savedInstanceState == null`**.
- Liste → Détail : `replace(...).addToBackStack("detail")`.
- Détail → Carte : `replace(...).addToBackStack("carte")`.
- Retour matériel ou bouton « Retour au détail » : `popBackStack()`.
- `addOnBackStackChangedListener` met à jour le titre de l'action bar.

## 7. Filtrage

- `ContactFilter.apply()` : `trim().lowercase(Locale.getDefault())`, recherche `contains` sur
  nom, prénom et téléphone ; requête vide → liste complète.
- La RecyclerView soumet la liste filtrée via `ListAdapter.submitList()` (DiffUtil).
- Le clic transmet `contact.id` : aucun risque d'indice décalé.
- Un `TextView` « aucun contact » apparaît quand le résultat est vide.

## 8. Vérification manuelle (check-list TP)

- [ ] Filtrer par nom / prénom / téléphone, avec et sans majuscules, puis effacer.
- [ ] Ouvrir un contact depuis une liste filtrée (bonne fiche affichée).
- [ ] Modifier nom/téléphone/e-mail/photo/coordonnées, enregistrer, retour liste actualisée.
- [ ] Annuler : anciennes valeurs conservées.
- [ ] Appeler, e-mail, galerie, caméra (y compris annulations et absence d'application).
- [ ] Coordonnées absentes, non numériques, hors limites (`±90`, `±180`).
- [ ] Carte : marqueur avec le nom, centrage, retour au détail.
- [ ] Rotation de l'écran à chaque écran, puis navigation Retour.
- [ ] Après `git add`, vérifier que `local.properties` n'apparaît pas.

Le compte rendu détaillé et les réponses aux questions sont dans `RAPPORT.md`.
