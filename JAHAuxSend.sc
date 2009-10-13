JAHAuxSend{
	classvar <>allSends;
	
	var <>inbus,<>outbus;
	var <>sendGroup;
	var <>powerButt,<>volSlider;
	var <>busList;
	var <>outVol;
	var <>synth;
	var <>track;
	var <>ampSpec;
	var <>itemBase;
	var <>auxBusArray;
	var <>pre;
	
	*new{|parent,sendGroup,inbus|
		^super.new.initJAHAuxSend(parent,sendGroup,inbus);
	}
	
	initJAHAuxSend{|argparent,argsendGroup,arginbus|
		
		//SynthDef
/*		SynthDef(\jahAuxSend,{|inbus,outbus=0,vol=1.0,gate=1|
			var input,env;
			env = EnvGen.kr(Env([0,1,0],[0.01,0.01],\linear,1),gate,doneAction:2);
			input = In.ar(inbus,1);
			Out.ar(outbus,env*(input*vol));
		}).load(Server.default);*/
		track = argparent;
		inbus = arginbus;
		sendGroup = argsendGroup;
		itemBase = [\NONE,"-"];
		outVol = 0;
		ampSpec = \amp.asSpec;
		pre = true;
		
		SCStaticText(track.trackView,Rect(0,0,40,15))
		.font_(Font("Helvetica",10))
		.stringColor_(track.trackColor)
		.string_("send");
		
		volSlider = Knob(track.trackView,Rect(0,0,20,20))
					.canFocus_(false)
					.action_({|sl|
						try{
							outVol = ampSpec.map(sl.value);
							outVol.postln;
							synth.set(\vol,outVol);
						}
		});	
		
		busList = SCPopUpMenu(track.trackView,Rect(0,0,70,15))
					.background_(Color.grey)
					.font_(Font("Helvetica",9))
					.canFocus_(false)
					.items_(itemBase++auxBusArray)
					.action_({|menu|
						outbus = JAHDesk.auxBusDict.at(menu.item.asSymbol);
						synth.set(\outbus,outbus);
					});
		powerButt = RoundButton(track.trackView,Rect(0,0,15,15))
					.canFocus_(false)
					.extrude_(false)
					.border_(1)
					.states_([[\power,Color.black,Color.white],[\power,Color.red,Color.white]])
					.mouseDownAction_({|butt|
						if(butt.value==0){
							this.powerOn;
						}{
							this.powerOff;
						}
		});
	}
	
	powerOn{
		var action;
		if(pre ==true){
			action = \addToHead;
		}{
			action = \addToTail;
		};
		synth = Synth(\JAHAuxInput,[\inbus,inbus,\outbus,outbus,\vol,outVol],sendGroup,action);
	}
	powerOff{
		synth.free;
	}
	updateAuxArray{|newArray|
		busList.items_(itemBase++newArray);
	}	
}