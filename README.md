Cook4
============================

Cook4 is an Android app for sharing food with neighbors or people nearby. 


## Description

With Cook4, anyone can visualize nearby food offers or search them by name/tags/location. Selling food requires authorization, managed by the built-in Privilege API. P2P payments are handled with PayPal, using the Adaptive Payments API.

## Configuration

The project is composed of two modules: the Android frontend with root ```Cook4-android/``` and the backend with root ```Cook4-server/```.

To build and run the Android client, first edit and move the file ```res/config.xml``` to the folder ```res/values```. The file contains some configuration data (such as API keys) without which some app features (like the login with Google/Facebook) will not work properly. Communications with the backend use a self-signed SSL certificate contained in the ```assets/```folder.

The Java backend is set to run inside Glassfish, but any similar web container would probably be fine as well. A JDBC-compatible database needs to be up and running before deploying. The following configuration files need to be edited and renamed by cutting the ```_example``` suffix:

* ```Configs.java``` (various app settings: server URL, PayPal credentials, Google/FB keys, debug email)
* ```glassfish-resources.xml``` (db location, connection pools)
* ```persistence.xml``` (db credentials, JPA provider, tables generation)
* ```load-script.sql``` (password for admin tasks)



## Contributing

Contributions are welcome, preferably after discussing the new features inside the Issues section.

## Download

You can download Cook4 from the [Play Store](https://play.google.com/store/apps/details?id=com.beppeben.cook4)

![Cook4 logo](https://lh3.googleusercontent.com/TOrDqUx9iCiF_iu3uWrEpyaxRrgvt2V4s-KdS1OJkDNNYf4MIuS72ZE0p2PupTsPd8wd=h900 "Cook4")
