import studio.forface.easygradle.dsl.*

/**
 * The list of the developers working on the Projects
 * @author Davide Farella
 */
val PublishConfig.applyDevelopers: PublishConfig.DevelopersBuilder.() -> Unit get() = {
    developers {

        developer {
            id = "4face"
            name = "Davide Farella"
            email = "4face91@protonmail.com"
        }

        developer {
            name = "Mateusz Markowicz"
            email = "poniekad@protonmail.com"
            id = email
        }

        developer {
            name = "Algirdas Pundzius"
            email = "algirdas.pundzius@protonmail.com"
            id = email
        }

        developer {
            name = "Dino Kadrikj"
            email = "kadrikj@protonmail.com"
            id = email
        }
    }
}
