=============
Rhythos RPG Game and Editor
=============

Currently, Rythos is a Haxe/NME action battle game which you can play and read about here: http://fancyfishgames.com/rhythos/

However, my plan is to create a full-featured, easy to use RPG Maker-like game editor that will eventually allow you to create and customize entire RPGs on top of the Rhythos battle system, and export the game in Flash, HTML5, and Native targets. Future battle systems are also possible. There's a long way to go, but I have included and open-sourced a map editing and project management application I wrote a few years back, and am currently in the process of converting and updating to be the full game editor I envisioned.

Stay tuned for updates, and if you want to support the project, feel free to contact me!

Building and running
--------------------

First, in rhythos/game, open Rhythos.hxproj in Flash Develop and build a release flash version of the base code. Only the flash release option currently works, the other options will require some work to fix platform-dependent differences. If you don't have Flash Develop, you can get it, or use another text editor. Haxe/NME and all dependencies can be found here: http://www.nme.io/

Copy the created rhythos/game/bin/flash/bin/base.swf into rhythos/editor/src, or in eclipse, you can use the Link Source option to automatically link the base.swf as part of the generated source.

Next, in rhythos/editor, import the project into Eclipse, in MapEditor.main uncomment SWFTarget.test(), then compile and run MapEditor. It will run the map editor (still under development), and in the console, it will print updates on the conversion of all assets and creation of the swf. This may take some time the first time you run the code. When it prints "Done," you can close the MapEditor window (which will end the program), and you should see an out.swf in rhythos/editor. This used the base.swf we created from rhythos/game, and added all of the art and music assets and created something that runs very similarly to the publicly uploaded version (it's certainly playable, with a few small bugs as this process is still under development).

The generation of out.swf can be disabled by commenting out ExportSWF.test(); in the main function. Feel free to mess with the MapEditor, but I am currently in the process of switching it over to a nicer system of resource management linked with files, and so importing, exporting, opening and saving will be disabled.

This will be easier later, I promise!

TODO
--------------------

Right now, the "database" of enemies, equipment, etc is actually in rhythos/game/src/com/davidmaletz/mrpg/Project.hx, while the actual art and sound assets are added in ExportSWF.text(). However, the plan is to have all of the editing done in MapEditor, and then ExportSWF will add all of the database objects, art and sound into the swf file upon testing and running. Everything will be handled in the java side editor, and the game haxe code will just handle reading and managing the passed assets. Eventually, I plan to add other Export classes for other platforms as well. It will be a lot of work, but it'll be really cool when it's done, and the map editing functionality is already well polished in the old MapEditor code, and the game code already includes a lot of useful functionality like the battle system, dialog boxes and choice dialogs.

Check back in the next few days, and hopefully I'll have something a lot more usable, but for now, feel free to check out the code, and get in touch if you want to be a contributor!