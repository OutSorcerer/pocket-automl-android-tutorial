# Pocket AutoML: Android App Creation Tutorial

## Translations

* [English](README.md) (this document)
* [Русский](README_ru.md)

## Overview

This document will walk you through the steps for creating your Android app that runs a deep learning image classification model trained in [Pocket AutoML](https://play.google.com/store/apps/details?id=com.evgeniymamchenko.pocketautoml) and exported in TensorFlow Lite format. The app will continuously classify whatever it sees from the device's back camera. 

This tutorial is based on [TensorFlow Lite image classification Android example application](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android). 
For an explanation of its source code, see
[Explore the code](EXPLORE_THE_CODE.md).

> If you have any issues following this tutorial please contact me (the creator of Pocket AutoML) via [email](mailto:pocket-automl@evgeniymamchenko.com) at or by creating a GitHub [issue](https://github.com/OutSorcerer/pocket-automl-android-tutorial/issues). 

## Requirements

* [Android Studio](https://developer.android.com/studio) 4.2 (installed on a Linux, Mac or Windows machine)

* (if not using an emulator) an Android device in
  [developer mode](https://developer.android.com/studio/debug/dev-options)
  with USB debugging enabled and a USB cable (to connect Android device to your computer)

## Step 1. Train a model in Pocket AutoML 

* [Install Pocket AutoML from Google Play Store](https://play.google.com/store/apps/details?id=com.evgeniymamchenko.pocketautoml) and open it

* Create a task e.g. `Kittens or Puppies` by pressing `+` button

* Create a class e.g. `Kittens`
  
  <img src="images/task_with_classes.png?raw=true" style="width: 50%" />

* Add example images of the class by taking photos with a camera or picking them from a storage
  
  <img src="images/kitten_images.png?raw=true" style="width: 50%" />

* Go back to the task view by pressing `<-` and repeat these steps for each class

* Go back to the task view by pressing `<-`, switch to the `MODEL` tab and press `TRAIN`

  <img src="images/model_training.png?raw=true" style="width: 50%" />

## Step 2. Export a model in TF Lite format from Pocket AutoML

* Press `EXPORT IN TENSORFLOW LITE FORMAT`

* Swipe down on the status bar at the top of the screen to open the notification drawer and track the export progress. The export takes few minutes.

  <img src="images/export_notification_progress.png?raw=true" style="width: 50%" />

* When the export is done, press `Share Model` on a notification to open the standard Android Sharesheet, chose a sharing method to send a model to your PC (e.g. send it to yourself via an email app like GMail or store it on your cloud storage like Google Drive or Dropbox)
  
  <img src="images/export_notification_share.png?raw=true" style="width: 50%" />

## Step 3. Clone the Pocket AutoML example source code

Run the following command to get the demo application.

```
git clone https://github.com/OutSorcerer/pocket-automl-android-tutorial
```

Open the example source code in Android Studio. To do this, open Android
Studio and select `Open an existing project`, setting the folder to
`pocket-automl-android-tutorial`

<img src="images/classifydemo_img1.png?raw=true" />

Unlike the original example, this one uses only [TFLite Support library](https://www.tensorflow.org/lite/inference_with_metadata/lite_support) to avoid confusion. An alternative is [TensorFlow Lite Task Library](https://www.tensorflow.org/lite/inference_with_metadata/task_library/image_classifier), see the [README](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android#switch-between-inference-solutions-task-library-vs-support-library) of the original example for details.

## Step 4. Build the Android Studio project

Select `Build -> Make Project` and check that the project builds successfully.
The `build.gradle` file will prompt you to download any missing
libraries.

<img src="images/classifydemo_img4.png?raw=true" style="width: 40%" />

<img src="images/classifydemo_img2.png?raw=true" style="width: 60%" />

## Step 5. Install and run the app

>Follow this step to make sure that the example runs successfully in your environment using its built-in models. The following step will demonstrate how to add your custom model from Pocket AutoML into the example app.

### Run on a device

If you are willing to test the app on an Android device, connect the device to the computer and be sure to approve any ADB
permission prompts that appear on your phone. Click `Run -> Run 'app'` from the main menu of Android Studio. Select
the deployment target in the connected devices to the device on which the app
will be installed. This will install the app on the device.

### Run on an an emulator

If you are willing to test the app on an Android emulator
* select `Tools -> AVD Manager -> Create Virtual Device...`
* choose a device definition e.g. `Pixel 2` (this controls its screen resolution and density)
* click `Next` and select a system image, `Android 11 (API level 30)` is recommended, click `Download` on the selected system image, wait for download to complete, click `Next` and `Finish`
* close the `AVD Manager`, select the newly created device in a list of available devices and click `Run -> Run 'app'` from the main menu of Android Studio

If you want to know more, see [Create and manage virtual devices](https://developer.android.com/studio/run/managing-avds#createavd) in Android documentation.

<img src="images/classifydemo_img5.png?raw=true" style="width: 60%" />

<img src="images/classifydemo_img6.png?raw=true" style="width: 70%" />

<img src="images/classifydemo_img7.png?raw=true" style="width: 40%" />

<img src="images/classifydemo_img8.png?raw=true" style="width: 80%" />

To test the app, open the app called `Pocket AutoML Classify` on your device or emulator.
When you run the app the first time, the app will request permission to access the camera.
Re-installing the app may require you to uninstall the previous installations.

## Step 6. Add your model from Pocket AutoML into the example app

* At this point you must have `<your_task_name>.zip` file on your PC. Extract its contents into `pocket-automl-android-tutorial/models/src/main/assets`. You will have `<your_task_name>.tflite` and `<your_task_name>.labels.txt` there. 

* Open `ClassifierPocketAutoML.java` (by clicking `Navigate -> Search Everywhere` or pressing `Shift` twice and typing its name)

* Replace the implementation of `getModelPath` with `return "<your_task_name>.tflite";`

* Replace the implementation of `getLabelPath` with `return "<your_task_name>.labels.txt";`

* Run the app, swipe up the bottom sheet to expand it and select `Pocket_AutoML` from the `Model` dropdown menu

* You will see the the predicted class and the associated probability as a bold text under an image and probabilities of other classes below. Well done!

  <img src="images/pocket_automl_classify.png?raw=true" style="width: 50%" />

## Next steps

### Applications

Do you have a task at hand that can be solved with a help of an image classification model running in a mobile app? It could be sorting lego bricks or controlling a robot with hand gestures. 

I will be excited to know what you have built with the help of Pocket AutoML and this tutorial and will add links to Play Store or GitHub into this document.

### Other plarfotms

TF Lite can run not only on Android but on other platforms as well including [iOS](https://www.tensorflow.org/lite/guide/ios), [embedded Linux devices like Raspberry Pi or Coral](https://www.tensorflow.org/lite/guide/python) and [microcontrollers](https://www.tensorflow.org/lite/microcontrollers).

### Other model training methods

You can try other no-code or low-code deep learning solutions like [Teachable Machine](https://teachablemachine.withgoogle.com/), [Lobe](https://www.lobe.ai/) or [TensorFlow Lite Model Maker](https://www.tensorflow.org/lite/guide/model_maker).

Pocket AutoML uses transfer learning approach, you can also implement it yourself using a tutorial [Transfer learning and fine-tuning](https://colab.research.google.com/github/tensorflow/docs/blob/master/site/en/tutorials/images/transfer_learning.ipynb) in Google Colab.

### Learning deep learning

If you want to learn how to train better models and have a systematic understanding or deep learning I recommend [Deep Learning Specialization](https://www.coursera.org/specializations/deep-learning) and [Machine Learning Engineering for Production (MLOps) Specialization](https://www.coursera.org/specializations/machine-learning-engineering-for-production-mlops) on Coursera.

## Attribution statements

TensorFlow, the TensorFlow logo and any related marks are trademarks of Google Inc. Android is a trademark of Google LLC.

## License

[Apache License 2.0](LICENSE)
