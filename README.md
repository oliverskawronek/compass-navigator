# CompassNavigator
Simple navigation app for Android that works offline. Add your destinations at home and navigate only by GPS and compass without the need of the internet. The app shows you the direction you have to go and how far it is. At a minimum [API Level](http://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels) 11, Honeycomb is required. It was tested on LG E610. 
>**Note:** CompassNavigator is still under development. So please don't confide on it.

# How to build
1. Make sure you have installed
 * [Android SDK](https://developer.android.com/sdk/index.html)
 * [Maven](https://maven.apache.org/)
 * USB driver for your device, if you wich to test it without an emulator
3. The project uses the [Android Maven Plugin](http://simpligility.github.io/android-maven-plugin/). If you have trouble to run Maven, please consider the [Required Setup](http://simpligility.github.io/android-maven-plugin/#_required_setup) section of the plugin
4. Open a console and switch to the project folder (it contains the *pom.xml*)
5. Run `mvn clean install` to build the app
6. Run `mvn android:run` for starting the app

# How to use
1. Activate GPS
2. Add a destination: Press the **menu** button and select **Add destination**
3. Get a geo position: Use [mapcoordinates.net](http://www.mapcoordinates.net/en) or similiar sites to get latitude and longitude.
4. Enter name, latitude and longitude and press **Add**
5. Select a destination: Press the **Select destination** action in the action bar.
6. After the first geo position is available, the app shows you the direction and the distance you have to go.

![Add destination action](https://cloud.githubusercontent.com/assets/10528519/8078584/067f5a98-0f5f-11e5-9c2e-25250d881aa0.png)
![Enter destination details](https://cloud.githubusercontent.com/assets/10528519/8078582/067e2f88-0f5f-11e5-892e-d0895fa38c7a.png)
![Select destination](https://cloud.githubusercontent.com/assets/10528519/8078581/067dbf12-0f5f-11e5-99e3-238403623ebb.png)
![Navigate](https://cloud.githubusercontent.com/assets/10528519/8078583/067e3b7c-0f5f-11e5-94d2-6af9055de4cd.png)
