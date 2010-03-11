JAHInsertDict{
	classvar <>fx,<>gen,<>input,<>aux,<>master;
	
	*new{
		^super.new.initJAHInsertDict();
	}
	
	*initClass{
		
		fx = (\JAHComp:"Compressor",\JAHTremolo:"Tremolo",\JAHEnvFilter:"Envelope Filter",\JAHFreeVerb:"FreeVerb",\JAHOctave:"Octave");
		gen = (\JahTabletTgrain:"TGrains",\JahTabletSlice:"Tablet Slice",\JAHAudience:"audience");
		aux = (\JAHAuxInput:"Aux Input");
		input = (\JAHInput:"input");
		master = (\JAHAuxInputStereo:"Master Bus");
	}
	
	
}