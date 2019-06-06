## Idea
Android sound app that changes over time

## Technology
* LibPd http://libpd.cc/ Puredata as embedable library

## Apps with the same technology
* Honig - Orgon Remixer: https://ssl-account.com/cloud.residuum.org/index.php/s/nSOWFPLs332FHcb Remix of https://www.youtube.com/watch?v=tt19Qh8KyJ0
* Premier League Sonification: https://github.com/residuum/premier-league-sonification

## Current State

Github-Repo: https://github.com/residuum/Life-Soundtrack

Binary Download: https://ssl-account.com/cloud.residuum.org/index.php/s/YW2l1mz6lnqDXaH

In the Github repository, there is folder "pd" that contains the Pd patches. For running, you must open patch.pd and controller.pd in one instance of Pd, and then set a BPM rate via controller.pd and click the bang on the bottom right to load the samples and initialize the RNG. If [time] object cannot be instantiated, no problem, but the random numbers will be in the same sequence every time.

* Step detector liftet from https://github.com/SecUSo/privacy-friendly-pedometer/blob/master/app/src/main/java/org/secuso/privacyfriendlyactivitytracker/services/AccelerometerStepDetectorService.java
* Duration between steps are used to calculate BPM, clamped between 60 and 200 BPM (Weighted average of last 10 steps)
* 5 different layers (bass drum, pseudo-tom with frequency changed by sine LFO, 2 synths, loop/break player)
* every 4 beats, a parameter is changed (e.g. bass drum pattern, bass drum sound, sample fo loop player)
* every 8 beats, a layer is switched on or off, at least two are always playing
* State is saved to database and restored on restart

Basic sound app functions:
* Pauses on phone call and restarts after 
* Keeps running in the background, when minimized
* Notification icon, when sound is generated

## Puredata
The logic for Puredata is in the Github repository in the folder "pd". Load "patch.pd" and "controller.pd" in an instance of Pd (https://puredata.info ), set BPM in controller.pd and click on the bang (button) on the right side to load samples and initialize the RNG. If you get an error that [time] cannot be initialized, then the RNG will output the same sequence on every start.

## Simple to implement ideas

* Higher order Markov chains for generating synth melodies (currently only 1st order)
* More layers / synth implementations / samples / transition matrices
* Better sounds
* Animation reacting to sound on app interface, similar to https://ix.residuum.org/pd/denno.html (Low priority)
* Use not only equal probabilities

## Phone parameters to use
* GPS position: GPS position should influence the parameters, e.g. probabilities of triggering samples
* Simple: differenciate between Longitude / Latitude
* Difference between countries (OSM?)
* Difference between city and rural areas (OSM?)
* Time: 
* Use different samples / parameters in the morning than in the evening
* React to season (in combination with GPS), sample Wham on Christmas, ...
* Weather forecast? Other freely available data, maybe based on location? (e.g. https://github.com/residuum/PuRestJson/wiki/Using-JSON-Data-as-Pitch-Generator, https://github.com/residuum/PuRestJson/wiki/Using-Pd-as-Twitter-Client, 
* Commonly available sensors: acceleration (3D), rotation (3D), Light (1D), all sensors: https://developer.android.com/reference/android/hardware/Sensor.html

## Other ideas
* Let users record samples for looper / break
* Analyse user recording in microphone for filling transition matrices (quite simple in Pd https://twitter.com/ResiduumMuc/status/811683435886612480 )
* Change the app over time: Pd patches are able to modify themselves, so over time a patch can add/remove objects. Not easy, but doable (dynamic patching).
* Not only the current state should be reflected in the app, but also history (Last year you were in the USA, your friend in Canada, even if everything else is the same between you two you get different sounds / probabilities).
* History should not only be possible enhancement, but also degredation, maybe after some the app just outputs distorted noise, and then the user will have to remove and reinstall the app to start over, but history is lost.
* Different set of rhythms / samples / Markov chains as groups ("tracks"), combined with the different parameters.
