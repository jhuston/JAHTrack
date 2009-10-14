//JAHfxParams: container class to hold parameters for fx gui and midi control.
JAHfxParams{
	var <>name,<>param,<>label,<>val,<>spec,<>control;
	
	*new{|params|
		^super.new.initJAHfxParams(params);
	}
	
	initJAHfxParams{|argparams|

	}
	
	relateControl_{|argcontrol|
		control = argcontrol;
	}
}