PDFtoAudioBook
==============

This is a prototype of an Android app which takes a pdf and creates an
AudioBook. 

Dependancies on the Device
----------------------------
* [Android 2.2] the device should be running 2.2 or better
* [TextToSpeech] is turned on (see Settings)
* to have comercial quality audio you can use 
Loquendo it's a commercial voice which costs about 5-6$ (search Market)
* [Voice Recognition] is turned on (see Settings)
* [Internet Connection] You need an internet connection to use the Voice
Recognizer, and to extract the text from the PDF.
* [File Manager] a file manager such as OI File Manager (search Market) 
* [Mind Mapping] a mind mapping app such as Thinking Space (search market)

Library Dependancies
--------------------
* None

Contributing
-------------

Want to contribute? Great!

1. Fork it
2. Create a branch (git checkout -b my_copy)
3. Commit your changes (git commit -am "added some more regex to the text
cleaner")
4. Push to the branch (git push origin my_copy)
5. Create an Issue with a link to your branch

Instalation using Eclipse
-------------------------
* Download the source code
* Create an Android project and import the source files
* Install it onto your device 

Using the application
---------------------
* Check the device settings for Voice input & output to be sure that TextToSpeech
and VoiceRecognition are enabled
* Save some PDFs onto your device via the internet or SDCARD
* Open the file manager, browse to your pdf, click on it and a menu will appear
allowing you to register the pdf into your audio book list
* Alternatively, you can open the application and listen to the instructions 


PDFtoAudioBook is released under the Apache License.
