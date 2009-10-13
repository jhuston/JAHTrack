JAHTrackInput{
	classvar <>allInputs;
	
	var <>inbus,<>outbus,<>inputGroup;
	var <>inputList;
	var <>track;
	var <>powerButt,<>gainKnob;
	var <>parentView;
	var <>inputArray,<>itemBase;
	var <>synth;
	var <>gain;
	
	*new{|parent,inbus,outbus,inputGroup|
		^super.new.initJAHTrackInput(parent,inbus,outbus,inputGroup);
	}
	
	initJAHTrackInput{|argparent,arginbus,argoutbus,argGroup|

		SynthDef(\jahTrackInput,{|inbus=0,outbus,vol|
			var input;
			input = SoundIn.ar(inbus);
			Out.ar(outbus,input*vol);
			}).load(Server.default);

		track = argparent;
		gain = 1.0;
		parentView = track.trackView;
		inbus = arginbus ?? 8;
		outbus = argoutbus;
		inputGroup = argGroup;
		itemBase = [\NONE,"-"];
		inputArray = [\1,\2,\3,\4,\5,\6,\7,\8];
		inputList = PopUpMenu(parentView,Rect(0,0,70,15))
					.background_(Color.grey)
					.font_(Font("Helvetica",9))
					.canFocus_(false)
					.items_(itemBase++inputArray)
					.action_({|menu|
						if(menu.item != \NONE){
							inbus = (menu.item.asInteger)-1;
							synth.set(\inbus,inbus);
						}{
							powerButt.valueAction_(0);
						};
					});
					
		powerButt = RoundButton(parentView,Rect(0,0,15,15))
				.states_([[\power,Color.black,Color.white],[\power,Color.red,Color.white]])
				.canFocus_(false)
				.extrude_(false)
				.border_(1)
				.action_({|butt|
					butt.value.postln;
					if(butt.value == 1){
						this.powerOn();
					}{
						this.powerOff();
					};
				})
				.mouseDownAction_({|butt|
					powerButt.valueAction_(butt.value);
				});
		}
	powerOn{
		synth = Synth(\jahTrackInput,[\inbus,inbus,\outbus,outbus,\vol,gain],inputGroup,\addToHead);
	}
	
	powerOff{
		synth.free;
	}
}