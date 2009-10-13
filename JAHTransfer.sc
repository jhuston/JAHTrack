JAHTransfer{
	var <>outSlider;
	var <>track;
	var <>synth;
	var <>powerButt;
	var <>ampSpec;
	var <>outVol;
	var <>outputList;
	var <>outputArray;
	var itemBase;
	*new{|parent|
		^super.new.initJAHTransfer(parent);
		}
		
	initJAHTransfer{|argparent|
//synthdef
		SynthDef(\jahTransfer2x2,{|inbus,outbus=0,vol=1.0,gate=1|
			var input,env;
			env = EnvGen.kr(Env([0,1,0],[0.01,0.01],\linear,1),gate,doneAction:2);
			input = In.ar(inbus,1);
			Out.ar([outbus,outbus+1],env*(input*vol));
		}).load(Server.default);

		itemBase = ["NONE","-"];
		track = argparent;
		ampSpec = \amp.asSpec;
		track.trackView.startRow;
		outSlider = SmoothSlider(track.trackView,Rect(0,0,20,100))
					.canFocus_(false)
					.action_({|sl|
						try{
							outVol = ampSpec.map(sl.value);
							synth.set(\vol,outVol);
						}
					});
		track.trackView.decorator.gap_(4@4);
		SCStaticText(track.trackView,Rect(0,0,40,15))
		.font_(Font("Helvetica",10))
		.stringColor_(track.trackColor)
		.string_("volume");
		track.trackView.startRow;
		SCStaticText(track.trackView,Rect(0,0,80,15))
		.font_(Font("Helvetica",10))
		.stringColor_(track.trackColor)
		.string_("output");
		outputList = SCPopUpMenu(track.trackView,Rect(0,0,70,15))
					.background_(Color.grey)
					.font_(Font("Helvetica",9))
					.canFocus_(false)
					.items_(itemBase++outputArray)
					.action_({|menu|
						track.transOut = track.desk.masterBus.at(menu.item.asSymbol);
						synth.set(\outbus,track.transOut);
					});
	}
	
	powerOn{
		synth = Synth(\jahTransfer2x2,[\inbus,track.internalBus,\outbus,track.transOut,\vol,outVol],track.transGroup,\addToTail);
	}
	powerOff{
		synth.set(\gate,0);
	}
	updateOutArray{
		outputArray = track.desk.masterBus.keys.asArray.flat;
		outputList.items_(itemBase++outputArray);
	}
}