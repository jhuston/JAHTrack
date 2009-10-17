JAHFxMidi{
	classvar <uc33 = 32064556, <fcb1010 = -123428006;
	var <>ccList,<>ccNumArray;
	var fx;
	var device;

	*new{|fx,device,ccNumArray|
		^super.new.initJAHFxMidi(fx,device,ccNumArray);
	}
	
	initJAHFxMidi{|argfx,argdevice,argarray|
		ccList = List.new;
		ccNumArray = argarray;
		fx = argfx;
		switch(argdevice)
			{\uc33}{device = JAHFxMidi.uc33;}
			{\fcb1010}{device = JAHFxMidi.fcb1010;}
			{\nil}{device = JAHFxMidi.uc33;};
		fx.effect.settingsDict.at(\params).postln;
		
		fx.effect.settingsDict.at(\params).do{|param,i|
			param.postln;
			ccList.add(
				CCResponder({|src,chan,num,val|
					val = fx.fxParams.at(param).spec.map(val/127);
					{fx.fxParams.at(param).control.valueAction_(val)}.defer;
				},
				device,
				nil,
				ccNumArray[i],
				nil
				)
			);
		};
	}
}