# Pocket AutoML: руководство по созданию Android приложения для классификации изображений с помощью deep learning

## Переводы

* [English](README.md) 
* [Русский](README_ru.md) (этот документ)

## Обзор

Этот документ содержит шаги, следуя которым Вы создадите Ваше Android приложение, которое будет использовать deep learning модель классификации изображений, натренированную Вами в [Pocket AutoML](https://play.google.com/store/apps/details?id=com.evgeniymamchenko.pocketautoml) и экспортированную в формате TensorFlow Lite. Приложение будет непрерывно классифицировать изображения с тыловой камеры устройства.

Это руководство основано на [TensorFlow Lite image classification Android example application](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android). Чтобы лучше разобраться в коде, см. [Explore the code](EXPLORE_THE_CODE.md).

> Если у Вас возникли трудности при следовании этому руководству, напишите мне (создателю Pocket AutoML) на [электронную почту](mailto:pocket-automl@evgeniymamchenko.com) или создайте [issue](https://github.com/OutSorcerer/pocket-automl-android-tutorial/issues) на GitHub.

## Требования

* [Android Studio](https://developer.android.com/studio) 4.2 (установленная на машине под Linux, Mac или Windows)

* Android устройство в [режиме разрабочика](https://developer.android.com/studio/debug/dev-options) с включенной отладкой по USB и USB кабель (чтобы подключить Android устройство к вашей машине, если не используется эмулятор)

## Шаг 1. Натренируйте модель в Pocket AutoML

* [Установите Pocket AutoML в Google Play Store](https://play.google.com/store/apps/details?id=com.evgeniymamchenko.pocketautoml) и запустите его

* Создайте задачу, например `Котята или щенки`, нажав кнопку `+`

* Создайте класс, например `Котята`
  
  <img src="images/ru/task_with_classes.png?raw=true" style="width: 50%" />

* Добавьте примеры изображений класса, сделав фото или выбрав их на хранилище Вашего устройства.
  
  <img src="images/ru/kitten_images.png?raw=true" style="width: 50%" />

* Вернитесь на страницу задачи, нажав `<-` и повторите шаги выше для каждого класса

* Вернитесь на страницу задачи, нажав `<-`, перейдите на вкладку `МОДЕЛЬ` и нажмите `ТРЕНИРОВАТЬ`

  <img src="images/ru/model_training.png?raw=true" style="width: 50%" />

## Шаг 2. Экспортируйте модель в формате TF Lite из Pocket AutoML

* Нажмите `ЭКСПОРТИРОВАТЬ В ФОРМАТЕ TF LITE`

* Проведите вниз по строке состояния в верхней части экрана, чтобы открыть панель уведомлений и отслеживать прогресс экспорта. Процесс экспорта занимает несколько минут.

  <img src="images/ru/export_notification_progress.png?raw=true" style="width: 50%" />

* Когда экспорт завершён, нажмите `Поделиться моделью` на уведомлении, чтобы открыть стандартный интерфейс Android для обмена файлами, выберите метод обмена, чтобы послать файл модели на ваш ПК (например, пошлите его себе с помощью клиента электронной почты вроде GMail или сохраните его на облачном хранилище вроде Яндекс Диска или Dropbox)
  
  <img src="images/ru/export_notification_share.png?raw=true" style="width: 50%" />


## Шаг 3. Клонируйте репозиторий с кодом примера 

Выполните следующую команду, чтобы получить демо приложение.

```
git clone https://github.com/OutSorcerer/pocket-automl-android-tutorial
```

Откройте код примера в Android Studio. Для этого сперва запустите Android Studio,
выберите `Open an existing project`, затем выберите папку `pocket-automl-android-tutorial`.

<img src="images/classifydemo_img1.png?raw=true" />

В отличие от изначального примера, этот использует только [TFLite Support library](https://www.tensorflow.org/lite/inference_with_metadata/lite_support), чтобы избежать путаницы. Альтернативой является [TensorFlow Lite Task Library](https://www.tensorflow.org/lite/inference_with_metadata/task_library/image_classifier), см. [README](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android#switch-between-inference-solutions-task-library-vs-support-library) изначального примера.

## Шаг 4. Постройте проект в Android Studio

Выберите `Build -> Make Project` и убедитесь, что проект успешно строится. 
`build.gradle` запросит у Вас разрешение скачать отсутствующие компоненты.

<img src="images/classifydemo_img4.png?raw=true" style="width: 40%" />

<img src="images/classifydemo_img2.png?raw=true" style="width: 60%" />

## Шаг 5. Установите и запустите приложение

>Выполните этот шаг, чтобы убедиться, что пример успешно работает в вашей среде со стандартными моделями. 
Следующий шаг продемонстрирует, как добавить в пример Вашу модель, натренированную в Pocket AutoML.

### Запуск на устройстве

Если Вы хотите тестировать приложение на настоящем Anroid устройстве, подключите устройство к компьютеру и одобрите все разрешения на отладку, которые появляются на экране Вашего устройства.
Нажмите `Run -> Run 'app'` в главном меню Android Studio. 
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

## Шаг 6. Добавьте Вашу модель из Pocket AutoML в пример

* На данный момент у Вас на ПК должен быть файл `<имя_вашей_задачи>.zip`. Разархивируйте его содержимое в папку `pocket-automl-android-tutorial/models/src/main/assets`. Там должны появиться файлы `<имя_вашей_задачи>.tflite` и `<имя_вашей_задачи>.labels.txt`. 

* Откройте `ClassifierPocketAutoML.java` (нажав `Navigate -> Search Everywhere` или нажав `Shift` дважды и напечатав его название)

* Замените реализацию метода `getModelPath` на `return "<имя_вашей_задачи>.tflite";`

* Замените реализацию метода `getLabelPath` на `return "<имя_вашей_задачи>.labels.txt";`

* Запустите приложение, проведите вверх от низа экрана, чтобы раскрыть панель и выберите `Pocket_AutoML` из выпадающего меню `Model`

* Вы увидите предсказанный класс и соответствующую ему вероятность как полужирный текст вверху панели и вероятности других классов ниже. Отличная работа!

  <img src="images/ru/pocket_automl_classify.png?raw=true" style="width: 50%" />

## Следующие шаги

### Приложения

Есть ли у Вас на примете задача, которая может быть решена с помощью модели классификации изображений в мобильном приложении? Это может быть сортировка деталей Lego или управление роботом с помощью жестов.

Я буду рад узнать, что вы создали с помощью Pocket AutoML и этого руководства, и добавлю в этот документ ссылки на Play Store или GitHub.

### Другие платформы

TF Lite может работать не только на Anroid но и на других платформах включая [iOS](https://www.tensorflow.org/lite/guide/ios), [встроенные Linux устройства вроде Raspberry Pi или Coral](https://www.tensorflow.org/lite/guide/python) и [микроконтроллеры](https://www.tensorflow.org/lite/microcontrollers).

### Другие способы тренировки моделей

Вы можете попробовать другие no-code или low-code deep learning решения вроде [Teachable Machine](https://teachablemachine.withgoogle.com/), [Lobe](https://www.lobe.ai/) или [TensorFlow Lite Model Maker](https://www.tensorflow.org/lite/guide/model_maker).

Pocket AutoML использует подход transfer learning, Вы можете сами реализовать его используя руководство [Transfer learning and fine-tuning](https://colab.research.google.com/github/tensorflow/docs/blob/master/site/en/tutorials/images/transfer_learning.ipynb) в Google Colab.

### Изучение глубокого обучения

Если Вы хотите узнать, как лучше тренировать модели и иметь систематическое понимание глубокого обучения я рекомендую [Deep Learning Specialization](https://www.coursera.org/specializations/deep-learning) и [Machine Learning Engineering for Production (MLOps) Specialization](https://www.coursera.org/specializations/machine-learning-engineering-for-production-mlops) на Coursera.

## Attribution statements

TensorFlow, the TensorFlow logo and any related marks are trademarks of Google Inc. Android is a trademark of Google LLC.

## Лицензия

[Apache License 2.0](LICENSE)
