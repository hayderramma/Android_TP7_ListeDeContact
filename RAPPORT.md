# Compte rendu - TP Android Contacts V3

Etudiant : ________________   Date : ________________
Projet : ContactsV3 - Kotlin, XML, AndroidX Fragment/RecyclerView, Google Maps.

## 1. Architecture des fragments

L application ne possede qu une seule activite, MainActivity. Son layout ne contient qu un FragmentContainerView (fragment_container). Tous les ecrans sont des fragments dynamiques :

| Fragment | Role | Layout |
|---|---|---|
| ListeContactsFragment | Recherche + RecyclerView, etat vide | fragment_liste.xml, item_contact.xml |
| DetailContactFragment | Consultation/edition, actions V2, validation GPS | fragment_detail.xml |
| CarteContactFragment | MapView, marqueur et centrage | fragment_carte.xml |

MainActivity ne cree la liste que si savedInstanceState == null. Le FragmentContainerView restaure les fragments deja presents apres rotation : aucun doublon n est empile. Les transactions utilisent replace() et addToBackStack() ; le retour materiel depile la pile et un addOnBackStackChangedListener met a jour le titre. La carte possede un bouton Retour au detail qui appelle popBackStack().

CarteContactFragment utilise une MapView avec delegation explicite du cycle de vie (onCreate, onStart, onResume, onPause, onStop, onSaveInstanceState, onLowMemory, onDestroyView) protegee par mapViewInitialized. Cela evite les doublons de fragments enfants SupportMapFragment lors des recreations.

## 2. Choix de communication entre fragments

- Bundle avec identifiant stable : la liste ouvre le detail avec DetailContactFragment.newInstance(contact.id). transmettre un id plutot qu une position de ligne garantit le bon contact apres filtrage.
- ViewModel partage au niveau de l activite : ContactsViewModel detient la collection en memoire. DetailContactFragment appelle update(contact) ; LiveData emet une nouvelle liste et la RecyclerView se rafraichit immediatement. Ce choix a ete prefere au Fragment Result API car il existe une source de verite unique qui doit survivre a la rotation : aucun resultat inter-fragments n est necessaire.
- DetailViewModel (propre au fragment) conserve le brouillon sous forme de texte brut : une saisie meme invalide survit a la rotation, et Annuler ne modifie jamais le ViewModel partage.
- Carte : le Bundle contient id, nom et coordonnees deja validees ; le fragment carte n a pas besoin d aceder au modele.

## 3. RecyclerView

La ListView de la V2 est remplacee par une RecyclerView avec ContactAdapter (ListAdapter + DiffUtil) et ContactViewHolder. Chaque ligne affiche photo, nom complet, prenom et telephone. Le clic transmet contact.id. submitList() calcule les differences : la liste filtree s affiche sans rechargement global.

## 4. Strategie de filtrage

ContactFilter.apply(contacts, query) est isole du Fragment et testable. Il applique trim().lowercase(Locale.getDefault()) puis contains sur nom, prenom et telephone. Une requete vide renvoie tous les contacts ; le TextView emptyTextView s affiche si le resultat est vide. Le clic sur une ligne filtree utilise l identifiant du Contact present dans la liste filtree, donc aucun erreur d indice.

## 5. Integration Google Maps

Le SDK play-services-maps fournit MapView. La cle est lue depuis local.properties (gitignore) par app/build.gradle.kts et injectee via manifestPlaceholders. Aucune cle n est versionnee ; la documentation indique comment la restreindre par nom de paquet et empreinte SHA-1. La carte centre avec CameraUpdateFactory.newLatLngZoom(position, 15f) et ajoute un marqueur portant le nom. Si les coordonnees sont absentes ou invalides, un message explicite remplace la carte. Aucune autorisation de localisation n est demandee.

## 6. Tests effectues

Tests unitaires (gradlew test) : requete vide, filtrage par nom avec casse differente, par prenom, par telephone, recherche sans resultat, validation des coordonnees (valides, valeurs limites, non numeriques, hors limites, champs vides).
A verifier sur appareil/emulateur : filtrage et effacement, selection dans une liste filtree, modification puis retour liste actualisee, annulation, Appel/E-mail/Galerie/Camera avec annulations, coordonnees absentes/non numeriques/hors limites, marqueur et centrage Maps, retour carte vers detail, rotation et bouton Retour.

## 7. Difficultes rencontrees

- TakePicture natif n ajoute pas FLAG_GRANT_WRITE_URI_PERMISSION : un contrat personnalise a ete ecrit et un FileProvider declare dans le manifeste et file_paths.xml.
- La persistance de l URI galerie depend du fournisseur : takePersistableUriPermission est tente dans un try/catch et ImageLoader fournit une image de repli.
- La rotation perdait les champs saisis : le brouillon texte dans DetailViewModel resout le probleme.
- Sans cle API, le projet doit tout de meme compiler : placeholder vide par defaut et message explicite cote carte.

## 8. Questions de comprehension

1. Activity vs Fragment : une Activity est le point d entree systeme (fenetre, cycle de vie propre) ; un Fragment est un module d interface reutilisable, heberge dans une Activity, avec son propre cycle de vie et capable d etre ajoute/remplace dynamiquement.
2. FragmentContainerView fournit un conteneur XML integre au FragmentManager (restauration et transactions fiables). addToBackStack() permet de revenir a l ecran precedent avec le bouton Retour au lieu de le detruire.
3. RecyclerView.Adapter cree et lie les vues aux donnees ; ViewHolder conserve les references aux vues d une ligne pour eviter les findViewById repetees lors du defilement.
4. En filtrant on ne modifie pas la collection source et transmettons l identifiant stable du Contact ; l Adapter travaille sur la liste filtree mais le clic ne depend jamais de sa position.
5. L identifiant reste stable alors que la position de ligne change apres filtrage, tri ou suppression ; il garantit l ouverture du bon contact.
6. Stocker la cle uniquement dans local.properties ignore par Git, la lire au build via manifestPlaceholders, ne jamais committer le fichier, et la restreindre cote Google Cloud (SHA-1 + nom de paquet).

## 9. Barème indicatif

Fonctionnalites V2 conservees 4 ; fragments et navigation 4 ; RecyclerView et filtrage 4 ; GPS et Google Maps 4 ; robustesse et tests 2 ; GitHub, captures et compte rendu 2. Total 20.
