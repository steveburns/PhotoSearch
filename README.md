# PhotoSearch
Android app for searching images on imgur, which is an image sharing community site.
The user experience is very straightforward - simply type in a search term and get back results. Clicking an item displays the full-size image.

As for the code, it's 100% written in Kotlin and hopefully is a good reference for what it does and how it's structured architecturally.

It shows how you might choose to implement infinite scroll on Android.
Additionally, it attempts to show how you might separate your code into View, Presenter, and Model concerns.

There are many architectural approaches in the mobile community so I think it's important to pick one that is well suited for the maturity of your app.
Having said that, I believe most apps of any substance will benefit from separating code into the following areas as I've done in this project:
1. View
2. Presenter
3. Model
4. Navigation
