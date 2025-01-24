# Scalix - Manipulation de Données et Interopérabilité JSON avec Scala

## Auteurs
- **Matthis BLEUET**
- **Baptiste LABORDE**

## Description du projet
Scalix est une application Scala qui utilise l'API de TMDB pour effectuer des requêtes complexes sur les acteurs, réalisateurs, et films. L'objectif principal est de manipuler des données en JSON de manière efficace, tout en minimisant les appels réseau. 

Le projet a été conçu avec une architecture fonctionnelle, exploitant les immutabilités et les compréhensions Scala, tout en appliquant des optimisations via le caching.

## Fonctionnalités principales
1. **Recherche d'identifiants d'acteurs :**
   - Méthode : `findActorId(name: String, surname: String): Option[Int]`
   - Permet de retrouver l'identifiant d'un acteur via TMDB.

2. **Récupération de la filmographie d'un acteur :**
   - Méthode : `findActorMovies(actorId: Int): Set[(Int, String)]`
   - Retourne la liste des films où l'acteur a joué, sous forme de paires (ID du film, Titre).

3. **Récupération du réalisateur d'un film :**
   - Méthode : `findMovieDirector(movieId: Int): Option[(Int, String)]`
   - Donne l'identité du réalisateur d'un film.

4. **Détection de collaborations :**
   - Méthode : `collaboration(actor1: FullName, actor2: FullName): Set[(String, String)]`
   - Trouve les collaborations entre deux acteurs en listant les paires (Réalisateur, Film).

5. **Caching des résultats :**
   - Réduction des appels API grâce à un cache en deux niveaux (fichiers locaux et dictionnaires en mémoire).

6. **Quiz (bonus) :**
   - Identification des paires d'acteurs ayant le plus souvent collaboré.

## Dépendances
Le projet utilise les bibliothèques suivantes :
- **json4s** pour la manipulation de JSON :
  ```scala
  libraryDependencies += "org.json4s" %% "json4s-ast" % "4.1.0-M9"
  libraryDependencies += "org.json4s" %% "json4s-native" % "4.1.0-M9"
  ```
## Pré-requis
1. Scala 3.3.4 et SBT installés.
2. Une clé API TMDB (disponible [ici](https://developer.themoviedb.org/docs/getting-started)). 
3. IDE IntelliJ (optionnel mais recommandé, avec le plugin Scala à jour).

## Installation
1. Clonez ce dépôt.
2. Ajoutez votre clé TMDB dans le code source.
3. Installez les dépendances via SBT :
```bash
   sbt update
   ```
4. Lancez le projet avec :
```bash
   sbt run
   ```
## Utilisation

Pour tester les fonctions, vous pouvez ajouter des instructions `println` comme dans les exemples suivants :
```scala
println(findActorId("Leonardo", "Dicaprio"))
println(findActorMovies(400))
println(findMovieDirector(11873))
println(collaboration(FullName("Tom", "Cruise"), FullName("Tom", "Hanks")))
```