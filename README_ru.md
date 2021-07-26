# Pocket AutoML: руководство по созданию Android приложения

## Переводы

* [English](README.md) 
* [Русский](README_ru.md) (этот документ)

## Обзор

Этот документ содержит шаги, следуя которым Вы создатите Ваше Android приложение, которое будет использовать модель, натренированную Вами в [Pocket AutoML](https://play.google.com/store/apps/details?id=com.evgeniymamchenko.pocketautoml) и экспортированную в формате TensorFlow Lite. Приложение будет непрерывно классифицировать изображения с тыловой камеры устройства.

Это руководство основано на [TensorFlow Lite image classification Android example application](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android). Чтобы лучше разобраться в коде, см. [Explore the code](EXPLORE_THE_CODE.md).

> Если у Вас возникли трудности при следовании этому руководству, напишите мне (создателю Pocket AutoML) на [электронную почту](mailto:pocket-automl@evgeniymamchenko.com) или создайте [issue](https://github.com/OutSorcerer/pocket-automl-android-tutorial/issues) на GitHub.

## Requirements

* [Android Studio](https://developer.android.com/studio) 4.2 (установленная на машине под Linux, Mac или Windows)

* (если не используется эмулятор) Android устройство в [режиме разрабочика](https://developer.android.com/studio/debug/dev-options) с включенной отладкой по USB и USB кабель (чтобы подключить Android устройство к вашей машине)

## Шаг 1. Клонируйте репозиторий с кодом примера 

Выполните следующий код, чтобы получить демо приложение.

```
git clone https://github.com/OutSorcerer/pocket-automl-android-tutorial
```

Откройте код примера в Android Studio. Для этого сперва запустите Android Studio,
выберете `Open an existing project`, затем выберите папку `pocket-automl-android-tutorial`.

<img src="images/classifydemo_img1.png?raw=true" />

В отличие от изначального примера, этот использует только [TFLite Support library](https://www.tensorflow.org/lite/inference_with_metadata/lite_support), чтобы избежать путаницы. Альтернативой является [TensorFlow Lite Task Library](https://www.tensorflow.org/lite/inference_with_metadata/task_library/image_classifier), см. [README](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android#switch-between-inference-solutions-task-library-vs-support-library) изначального примера.

## Шаг 2. Постройте проект в Android Studio

Выберите `Build -> Make Project` и убедитесь, что проект успешно строится. 
`build.gradle` запросит у Вас разрешение скачать отсутствующие компоненты.

<img src="images/classifydemo_img4.png?raw=true" style="width: 40%" />

<img src="images/classifydemo_img2.png?raw=true" style="width: 60%" />

## Шаг 3. Установите и запустите приложение

>Выполните этот шаг, чтобы убедиться, что пример успешно работает в вашей среде со стандартными моделями. 
Следующие шаги продемонстрируют, как добавить в пример Вашу модель, натренированную в Pocket AutoML.

### Запуск на устройстве

Если Вы хотите тестировать приложение на настоящем Anroid устройстве, подключите устройство к компьютеру и одобрите все разрешения на отладку, которые появляются на экране Вашего устройства.
Нажмите `Run -> Run app.` в главном меню Android Studio. 
Выберите из списка устройство, куда будет установлено приложение.

### Запуск на эмуляторе

Если Вы хотите тестировать приложение на Android эмуляторе 
* выберите `Tools -> AVD Manager -> Create Virtual Device...`
* выберите тип устройства, например `Pixel 2` (это управляет его разрешением экрана и плотностью пикселей)
* нажмите `Next` и выберите образ системы, рекомендуется `Android 11 (API level 30)`, нажмите `Download` на выбранном образе системы, подождите окончания скачивания, нажмите `Next` и `Finish`
* закройте `AVD Manager`, выберите только что созданное устройство в списке доступных устройств и нажмите `Run -> Run 'app'` в главном меню Android Studio

Если Вы хотите узнать больше, см. [Create and manage virtual devices](https://developer.android.com/studio/run/managing-avds#createavd) в документации Android.

<img src="images/classifydemo_img5.png?raw=true" style="width: 60%" />

<img src="images/classifydemo_img6.png?raw=true" style="width: 70%" />

<img src="images/classifydemo_img7.png?raw=true" style="width: 40%" />

<img src="images/classifydemo_img8.png?raw=true" style="width: 80%" />

Чтобы протестировать приложение, откройте приложение под названием `Pocket AutoML Classify` на Вашем устройстве или эмуляторе.
Когда Вы запускаете приложение впервые, оно запросит доступ к камере.
Переустановка приложения может потребовать удаление предыдущих его установок.

## Шаг 4. Натренируйте модель в Pocket AutoML

* [Установите Pocket AutoML в Google Play Store](https://play.google.com/store/apps/details?id=com.evgeniymamchenko.pocketautoml) и запустите его

* Создайте задачу, например `Котята или щенки`, нажав кнопку `+`

* Создайте класс, например `Котята`
  
  <img src="images/ru/task_with_classes.png?raw=true" style="width: 50%" />

* Добавьте примеры изображений класса, сделав фото или выбрав их на хранилище Вашего устройства.
  
  <img src="images/ru/kitten_images.png?raw=true" style="width: 50%" />

* Вернитесь на страницу задачи, нажав `<-` и повторите шаги выше для каждого класса

* Вернитесь на страницу задачи, нажав `<-`, перейдите на вкладку `МОДЕЛЬ` и нажмите `ТРЕНИРОВАТЬ`

  <img src="images/ru/model_training.png?raw=true" style="width: 50%" />

## Шаг 5. Экспортируйте модель в формате TF Lite из Pocket AutoML

* Нажмите `ЭКСПОРТИРОВАТЬ В ФОРМАТЕ TF LITE`

* Проведите вниз по строке состояния в верхней части экрана, чтобы открыть панель уведомлений и отслеживать прогресс экспорта. Это займет несколько минут.

  <img src="images/ru/export_notification_progress.png?raw=true" style="width: 50%" />

* Когда экспорт завершён, нажмите `Поделиться моделью` на уведомлении, чтобы открыть стандартный интерфейс Android для обмена файлами, выберите метод обмена, чтобы послать файл модели на ваш ПК (например, пошлите его себе с помощью клиента электронной почты вроде GMail или сохраните его на облачном хранилище вроде Яндекс Диска или Dropbox)
  
  <img src="images/ru/export_notification_share.png?raw=true" style="width: 50%" />

## Шаг 6. Добавьте Вашу модель в пример

* На данный момент у Вас на ПК должен быть файл `<имя_вашей_задачи>.zip`. Разархивируйте его содержимое в папку `pocket-automl-android-tutorial/models/src/main/assets`. Там должны появиться файлы `<имя_вашей_задачи>.tflite` и `<имя_вашей_задачи>.labels.txt`. 

* Откройте `ClassifierPocketAutoML.java` (нажав `Navigate -> Search Everywhere` или нажав `Shift` дважды и напечатав его название)

* Замените реализацию метода `getModelPath` на `return "<имя_вашей_задачи>.tflite";`

* Замените реализацию метода `getLabelPath` на `return "<имя_вашей_задачи>.labels.txt";`

* Запустите приложение, проведите вверх от низа экрана, чтобы раскрыть панель и выберите `Pocket_AutoML` из выпадающего меню `Model`

* Вы увидите предсказанный класс и соответствующую ему вероятность как полужирный текст вверху панели и вероятности других классов ниже. Отличная работа!

  <img src="images/ru/pocket_automl_classify.png?raw=true" style="width: 50%" />

## Attribution statements

TensorFlow, the TensorFlow logo and any related marks are trademarks of Google Inc. Android is a trademark of Google LLC.

## Лицензия

[Apache License 2.0](LICENSE)
