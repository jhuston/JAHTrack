JAHTransfer{
	var <>outSlider;
	var <>track;
	var <>synth,<>auxSynth;
	var <>powerButt;
	var <>ampSpec;
	var <>outVol,<>auxSendVol;
	var <>auxSendList,auxSendPowerButt,auxSendKnob;
	var <>auxBusArray;
	var <>auxSendBus;
	var itemBase;
	*new{|parent|
		^super.new.initJAHTransfer(parent);
		}
		
	initJAHTransfer{|argparent|
//synthdef
		SynthDef(\jahTransfer2x2,{|inbus,outbus=0,vol=1.0|
			var input;
			input = In.ar(inbus,1);
			Out.ar([outbus,outbus+1],input*vol);
		}).load(Server.default);

		itemBase = ["NONE","-"];
		track = argparent;
		auxSendVol = 1.0;
		ampSpec = \amp.asSpec;
		outSlider = SmoothSlider(track.trackView,Rect(0,0,50,200))
					.canFocus_(false)
					.action_({|sl|
						try{
							outVol = ampSpec.map(sl.value);
							synth.set(\vol,outVol);
						}
					});
		auxSendList = SCPopUpMenu(track.trackView,Rect(0,0,80,15))
					.background_(Color.grey)
					.font_(Font("Helvetica",9))
					.canFocus_(false)
					.items_(itemBase++auxBusArray)
					.action_({|menu|
						auxSendBus = JAHDesk.auxBusDict.at(menu.item.asSymbol);
					});
		auxSendPowerButt = RoundButton(track.trackView,Rect(0,0,15,15))
					.canFocus_(false)
					.extrude_(false)
					.border_(1)
					.states_([[\power,Color.black,Color.white],[\power,Color.red,Color.white]])
					.mouseDownAction_({|butt|
						if(butt.value==0){
							this.auxPowerOn;
						}{
							this.auxPowerOff;
						}
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
		synth = Synth(\jahTransfer2x2,[\inbus,track.internalBus,\outbus,track.transOut,\vol,outVol],track.transGroup,\addToTail);
	}
	powerOff{
		synth.free;
	}
	auxPowerOn{
		auxSynth = Synth(\JAHAuxInput,[\inbus,track.internalBus,\outbus,auxSendBus,\vol,auxSendVol],track.transGroup,\addToHead);
	}
	auxPowerOff{
		auxSynth.free;
	}
	updateAuxArray{
		auxBusArray = JAHDesk.auxBusDict.keys.asArray.flat;
		auxSendList.items_(itemBase++auxBusArray);
	}
}