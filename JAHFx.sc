//JAHFx superClass for inserts.
/*settingsDict = (\synthSelector:\jahInput,
				\params:[\vol],
				\label:["gain"],
				\val:[1.0],
				\guiType:[\knob],
				\spec:[[0.0,2.0,\linear].asSpec],
				\inbus:inbus,
				\outbus:outbus
				);*/
JAHFx{
	classvar <>allFX;
	
	var <>params;
	var <>settings;
	var <>settingsDict;
	var <>target;
	var <synth;
	var <>isOn;
	var <synthSelector;
	var <>synthArgs;
	var s;
	var <>jahGui;
	var <>parentInsert;
	var <>outbus,<>inbus;
	var <>currentPreset;
	
	*initClass{
		allFX = List.new;
	}
	*new{|target,inbus,outbus|
		^super.new.initJAHFx(target,inbus,outbus);
	}
	
	initJAHFx{|argtarget,arginbus,argoutbus|
		settingsDict =();
		s = Server.default;
		target = argtarget ?? nil;
		inbus = arginbus ?? 8;
		outbus = argoutbus ?? 0;
		isOn = false;
		currentPreset = nil;
		allFX.add(this);
	}
	
	setSynthArgs{
		settingsDict.inbus = inbus;
		settingsDict.outbus = outbus;
		synthArgs = [settingsDict.at(\params),settingsDict.at(\val)].flop.flat++[\inbus,inbus,\outbus,outbus];
	}
	on{
		"on.\n".postln;
		if(isOn == false){
			isOn = true;
			synth = Synth(synthSelector,synthArgs,target,\addToHead);
		}
	}
	
	off{
		"off\n".postln;
		synth.free;
		isOn = false;
	}
	remove{
		"remove".postln;
		this.removeGui;
		JAHFx.allFX.remove(this);
	}
	hideGui{
		jahGui.win.visible_(false);
	}
	removeGui{
		try{jahGui.win.close();};
		jahGui = nil;
	}
	gui{
			if(jahGui.isNil){
				^jahGui = JAHfxGui.new(settingsDict).fx_(this);
		}{^jahGui.win.front;}
	}
	saveSettings{
		Dialog.savePanel({|path|
			settingsDict.writeArchive(path);
		});
	}
	getSettings{|path|
		"load settings".postln;
		if(path.isNil){
			Dialog.getPaths({|paths|
				paths.do{|path|
					currentPreset = PathName.new(path).fullPath;
				};
				this.applySettings();
			},{"canceled".postln});
		}{
		settingsDict = Object.readArchive(path);
		this.applySettings();
		};
		
	}
	
	applySettings{
		settingsDict = Object.readArchive(currentPreset);
		settingsDict.params.do{|param,i|
			jahGui.paramDict.at(param).guiControl.valueAction_(settingsDict.val[i]);
			};
	}
	
}


//Envelope Filter
JAHEnvFilter : JAHFx{
	*new{
		^super.new.initJAHEnvFilter();
	}
	
	initJAHEnvFilter{
//\jahEnvFilter inbus=8,outbus=0,attack = 0.1, release =1.0, res = 0.5,mul = 2000,add = 100|
	SynthDef(\jahEnvFilter,{|inbus=8,outbus=0,attack = 0.1, release =1.0, res = 0.5,mul = 2000,add = 100,direct=0.0|
		var source,in;
		in = In.ar(inbus,1);
		source = MoogVCF.ar(in,Lag.kr(AmplitudeMod.kr(in,attack,release,mul,add),0.2),res);
		ReplaceOut.ar(outbus,source+Out.ar(outbus,in*direct));
	}).load(s);		

		synthSelector = \jahEnvFilter;
		settingsDict = (
			\synthSelector:\jahEnvFilter,
			\params:[\attack,\release,\res,\mul,\add,\direct],
			\label:["attack","release","resonance","multiply","base freq","direct"],
			\val:[0.5,0.5,0.8,2000,100,0.0],
			\guiType:[\knob,\knob,\knob,\knob,\knob,\knob],
			\spec:[
				[0.01,2.0,\lin].asSpec,
				[0.01,2.0,\lin].asSpec,
				[0.1,1.0,\lin].asSpec,
				[100.0,2000.0,\exponential].asSpec,
				[10.0,3000.0,\exponential].asSpec,
				\amp.asSpec
				],
			\inbus:inbus,
			\outbus:outbus
		);
	}
}

//Tremolo

JAHTremolo : JAHFx{

	*new{
		^super.new.initJAHTremolo();
	}
	
	initJAHTremolo{
		//synthdef
		SynthDef(\jahTremolo,{|inbus=8,outbus=0,rate=10.0,depth=1.0|
			var input,mod,offset;
			depth = depth/2;
			offset = 1-depth;
			input = In.ar(inbus);
			mod = SinOsc.ar(rate,0,depth,offset);
			ReplaceOut.ar(outbus,input*mod);
		}).load(s);
		
		synthSelector = \jahTremolo;
		//params
		settingsDict = (\synthSelector:\jahTremolo,
				\params:[\rate,\depth],
				\label:["rate","depth"],
				\val:[5.0,1.0],
				\guiType:[\knob,\knob],
				\spec:[[0,20,\linear].asSpec,[0,1].asSpec],
				\inubs: inbus,
				\outbus:outbus
				);

	}
}

JAHInput : JAHFx{
	
	*new{
		^super.new.initJAHInput();
	}
	
	initJAHInput{

		SynthDef(\jahInput,{|inbus=8,outbus,vol|
			var input;
			input = In.ar(inbus,1);
			Out.ar(outbus,input*vol);
			}).load(s);

		synthSelector = \jahInput;
		settingsDict = (\synthSelector:\jahInput,
						\params:[\vol],
						\label:["gain"],
						\val:[1.0],
						\guiType:[\knob],
						\spec:[[0.0,2.0,\linear].asSpec],
						\inbus:inbus,
						\outbus:outbus
						);
	}
}

JAHFreeVerb : JAHFx{
	
	*new{
		^super.new.initJAHFreeVerb();
	}
	
	initJAHFreeVerb{
		SynthDef(\JAHFreeVerb, {| inbus=0, outbus=0, mix=0.25, room=0.15, damp=0.5, fxlevel=0.75, level=0 | 
				var fx, sig; 
				sig = In.ar(inbus, 1); 
				fx = FreeVerb.ar(sig, mix, room, damp); 
				ReplaceOut.ar(outbus, (fx*fxlevel) + (sig * level)) // level 
				}).load(s);
	
	synthSelector = \JAHFreeVerb;
	settingsDict = (\synthSelector: \JAHFreeVerb,
					\params:[\mix,\room,\damp,\fxlevel,\level],
					\label:["mix","room","damp","fxLevel","dryLevel"],
					\val:[0.25,0.15,0.5,0.75,0.0],
					\guiType:[\knob,\knob,\knob,\knob,\knob],
					\spec:[\amp.asSpec,nil,nil,nil,nil],
					\inbus:inbus,
					\outbus:outbus
					);
	}
}

JAHAuxInput : JAHFx{
	
	*new{
		^super.new.initJAHAuxInput();
	}
	
	initJAHAuxInput{

		SynthDef(\JAHAuxInput,{|inbus,outbus,vol=1.0|
			var input;
			input = InFeedback.ar(inbus,1);
			Out.ar(outbus,input*vol);
			}).load(s);

	synthSelector = \JAHAuxInput;
	settingsDict = (\synthSelector: \JAHAuxInput,
					\params:[\vol],
					\labels:["volume"],
					\val:[1.0],
					\guiType:[\knob],
					\spec:[[0.0,2.0,\linear].asSpec],
					\inbus:inbus,
					\outbus:outbus
					);
	}
	
	
	
}