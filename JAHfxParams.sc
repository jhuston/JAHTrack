//JAHfxParams: container class to hold parameters for fx gui and midi control.
JAHfxParams{
	var <>name,<>param,<>label,<>val,<>spec,<>control;
	
	*new{|params|
		^super.new.initJAHfxParams(params);
	}
	
	initJAHfxParams{|argparams|
/*		if(argparams.notEmpty){
			name = argparams[0];
			param = argparams[1];
			label = argparams[2];
			val = argparams[3];
			spec = argparams[4];
		};*/
	}
	
	relateControl_{|argcontrol|
		control = argcontrol;
	}
}