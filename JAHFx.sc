//JAHFx superClass for inserts.
/*settingsDict = (\synthSelector:\jahInput,
				\params:[\vol],
				\label:["gain"],
				\val:[1.0],
				\guiType:[\knob],
				\spec:[[0.0,2.0,\linear].asSpec],
				\inbus:nil,
				\outbus:outbus
				);*/
JAHAbstractFx{
	classvar <>fxDictionary,<>s;
	*initClass{
		fxDictionary = ();
		s = Server.default;
		this.allSubclasses.do{|class|
			fxDictionary.put(class.name,class);
		};
	}
}

JAHFx{
	classvar <>allFX;
	
	var <>effect;
	var <>fxParams;
	var <>defaults;
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
	var <>currentValues;
	var <>paramNames;
	var <>presetsDict;
	
	*initClass{
		allFX = List.new;
	}
	*new{|type,target,inbus,outbus|
		^super.new.initJAHFx(type,target,inbus,outbus);
	}
	
	initJAHFx{|argtype,argtarget,arginbus,argoutbus|
		effect = JAHAbstractFx.fxDictionary.at(argtype).new;
		fxParams = ();
		defaults = effect.settingsDict;
		paramNames = effect.settingsDict.at(\params);
		currentValues = effect.settingsDict.at(\val);
		postf("JAHFX\n");
		synthSelector = defaults.synthSelector;

		if(File.exists("JAH/fxPresets/"++synthSelector.asString)){
			presetsDict = Object.readArchive("JAH/fxPresets/"++synthSelector.asString);
		}{presetsDict = ();};

		s = Server.default;
		target = argtarget ?? nil;
		inbus = arginbus ?? 8;
		outbus = argoutbus ?? 0;
		isOn = false;
		currentPreset = \NONE;
		defaults.params.do{|param,i|
			fxParams.put(param,JAHfxParams()
				.name_(defaults.synthSelector)
				.param_(param)
				.label_(defaults.label[i])
				.val_(defaults.val[i])
				.spec_(defaults.spec[i])
				);
			};
		allFX.add(this);
	}
	
	//TODO working on fxParams Logic...
	setSynthArgs{
		synthArgs = [];
		fxParams.collect({|item|
		synthArgs = synthArgs.add([item.param,item.val]);});
		synthArgs = synthArgs.flat;
		^synthArgs;
	}
	
	synthValues{
		
		
	}
	on{
		"on.\n".postln;
		if(isOn == false){
			isOn = true;
			synth = Synth(synthSelector,this.setSynthArgs++[\inbus,inbus,\outbus,outbus],target,\addToHead);
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
				jahGui = JAHfxGui.new(defaults).fx_(this);
				jahGui.initGUI();
		}{^jahGui.win.front;}
	}
	
	savePreset{|name|
		name = name.asSymbol;
		currentValues = nil;
		currentPreset = name;
		paramNames.do{|param,i| currentValues = currentValues.add(fxParams.at(param).val)};
		presetsDict.put(name,currentValues);
		presetsDict.writeArchive("JAH/fxPresets/"++synthSelector.asString);
		jahGui.presetList.items_(jahGui.itemBase++presetsDict.keys.asArray.flat.sort);
		jahGui.presetList.value_(jahGui.presetList.items.indexOf(currentPreset));
	}
	loadPreset{|name|
		name = name.asSymbol;
		if(presetsDict.includesKey(name)){
			currentValues = presetsDict.at(name);
			currentPreset = name.asSymbol;
		paramNames.do{|param,i| fxParams.at(param).val_(currentValues[i]);
			if(jahGui.notNil){
				fxParams.at(param).control.valueAction_(currentValues[i]);
				jahGui.presetList.value_(jahGui.presetList.items.indexOf(currentPreset));
			}{this.setSynthArgs;};
			};
		};
	}
	removePreset{|name|
		name = name.asSymbol;
		presetsDict.removeAt(name);
		presetsDict.writeArchive("JAH/fxPresets/"++synthSelector.asString);
		jahGui.presetList.items_(jahGui.itemBase++presetsDict.keys.asArray.flat.sort);
		}
	//TODO change this for new preset system
	applySettings{|argSynthArgs|
		if(argSynthArgs.isNil){
			synthArgs = Object.readArchive(currentPreset);
		}{
			synthArgs = argSynthArgs;
		};
			if(isOn == true){
				synth = Synth.replace(synth.nodeID,synthSelector,synthArgs++[\inbus,inbus,\outbus,outbus]);
			};
			if(jahGui.notNil){
				synthArgs.pairsDo{|a,b| this.fxParams.at(a).control.valueAction_(b);}
			}
	}
	
}


//Envelope Filter
JAHEnvFilter : JAHAbstractFx{
	var <>settingsDict;

	
	*new{
		^super.new.initJAHEnvFilter();
	}
	
	initJAHEnvFilter{
//\jahEnvFilter inbus=8,outbus=0,attack = 0.1, release =1.0, res = 0.5,mul = 2000,add = 100|
	SynthDef(\jahEnvFilter,{|inbus=8,outbus=0,attack = 0.1, release =1.0, res = 0.5,mul = 2000,add = 100,direct=0.0|
		var source,in;
		in = In.ar(inbus,1);
		source = MoogVCF.ar(in,Lag.kr(AmplitudeMod.kr(in,attack,release,mul,add),0.02),res);
		ReplaceOut.ar(outbus,source+Out.ar(outbus,in*direct));
	}).load(s);

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
				[100.0,5000.0,\exponential].asSpec,
				[10.0,3000.0,\exponential].asSpec,
				\amp.asSpec
				],
			\inbus:nil,
			\outbus:nil
		);
	}
}

//Tremolo

JAHTremolo : JAHAbstractFx{
	var <>settingsDict;	
	*new{
		^super.new.initJAHTremolo();
	}
	
	initJAHTremolo{
		postf("JAHTremolo\n");
		//synthdef
		SynthDef(\jahTremolo,{|inbus=8,outbus=0,rate=10.0,depth=1.0|
			var input,mod,offset;
			depth = depth/2;
			offset = 1-depth;
			input = In.ar(inbus);
			mod = SinOsc.ar(rate,0,depth,offset);
			ReplaceOut.ar(outbus,input*mod);
		}).load(s);
		
		//params
		settingsDict = (\synthSelector:\jahTremolo,
				\params:[\rate,\depth],
				\label:["rate","depth"],
				\val:[5.0,1.0],
				\guiType:[\knob,\knob],
				\spec:[[0,20,\linear].asSpec,[0,1].asSpec],
				\inbus: nil,
				\outbus:nil
				);

	}
}

JAHInput : JAHAbstractFx{
	var <>settingsDict;	
	*new{
		^super.new.initJAHInput();
	}
	
	initJAHInput{

		SynthDef(\jahInput,{|inbus=8,outbus,vol|
			var input;
			input = In.ar(inbus,1);
			Out.ar(outbus,input*vol);
			}).load(s);

		settingsDict = (\synthSelector:\jahInput,
						\params:[\vol],
						\label:["gain"],
						\val:[1.0],
						\guiType:[\knob],
						\spec:[[0.0,2.0,\linear].asSpec],
						\inbus:nil,
						\outbus:nil
						);
	}
}

JAHFreeVerb : JAHAbstractFx{
	var <>settingsDict;	
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
	
	settingsDict = (\synthSelector: \JAHFreeVerb,
					\params:[\mix,\room,\damp,\fxlevel,\level],
					\label:["mix","room","damp","fxLevel","dryLevel"],
					\val:[0.25,0.15,0.5,0.75,0.0],
					\guiType:[\knob,\knob,\knob,\knob,\knob],
					\spec:[\amp.asSpec,nil,nil,nil,nil],
					\inbus:nil,
					\outbus:nil
					);
	}
}

JAHAuxInput : JAHAbstractFx{
	var <>settingsDict;
	*new{
		^super.new.initJAHAuxInput();
	}
	
	initJAHAuxInput{

		SynthDef(\JAHAuxInput,{|inbus,outbus,vol=1.0|
			var input;
			input = In.ar(inbus,1);
			Out.ar(outbus,input*vol);
			}).load(s);

	settingsDict = (\synthSelector: \JAHAuxInput,
					\params:[\vol],
					\label:["volume"],
					\val:[1.0,2.0],
					\guiType:[\knob,\knob],
					\spec:[[0.0,2.0,\linear].asSpec],
					\inbus:nil,
					\outbus:nil
					);
	}
	
	
	
}

JAHComp : JAHAbstractFx{
	
	var <>settingsDict;
	*new{
		^super.new.initJAHComp();
	}
	
	initJAHComp{
	SynthDef(\JAHComp,{|inbus = 8, outbus = 20, vol = 1.0,thresh = 0.01,sens = 10,ratio = 1|
		var input;
		input = In.ar(inbus,1);
		input = Compander.ar(input,input,thresh,sens,ratio,0.01,0.01);
		Out.ar(outbus,input*vol);
	}).load(s);
	
	settingsDict = (\synthSelector:\JAHComp,
					\params:[\vol,\thresh,\sens,\ratio],
					\label:["volume","threshhold","sensitivity","ratio"],
					\val:[1.0,0.05,0.5,0.5],
					\guiType:[\knob,\knob,\knob,\knob],
					\spec:[\amp.asSpec,[0.01,5,\linear].asSpec,[0.1,2.0,\linear].asSpec,[0.1,2,\linear].asSpec],
					\inbus:nil,
					\outbus:nil);
	}
}

JAHOctave : JAHAbstractFx{
	var <>settingsDict;
	
	*new{
		^super.new.initJAHOctave();
	}
	
	initJAHOctave{
		SynthDef(\JAHOctave, {| inbus=8, outbus=0, pitch1=1, pitch2=1, vol1=0.25, vol2=0.25, dispersion=0, fxlevel=0.5, level=0 | 
				   var fx1, fx2, sig; 
				   sig = In.ar(inbus, 1); 
				   fx1 = PitchShift.ar(sig, 0.2, pitch1, dispersion, 0.0001);
				   fx2 = PitchShift.ar(sig, 0.2, pitch2, dispersion, 0.0001);
				   ReplaceOut.ar(outbus,  ( ((fx1 * vol1) + (fx2 * vol2)) * fxlevel) + (sig * level) ); 
				}).load(Server.default); 
		
		settingsDict = (\synthSelector:\JAHOctave,
					\params:[\pitch1,\vol1,\pitch2,\vol2,\dispersion,\fxLevel,\level],
					\label:["pitch 1","vol 1","pitch 2","vol 2","dispersion","fx level","level"],
					\val:[1,0.25,1,0.25,0,0.5,0],
					\guiType:[\knob,\knob,\knob,\knob,\knob,\knob,\knob],
					\spec:[[0.0,2.0,\linear,0.01,1].asSpec,\amp.asSpec,[0.0,2.0,\linear,0.01,1].asSpec,\amp.asSpec,\amp.asSpec,\amp.asSpec,\amp.asSpec],
					\inbus:nil,
					\outbus:nil
					)
	}
}