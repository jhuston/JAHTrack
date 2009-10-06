JAHInsertDict{
	classvar <>fx,<>gen,<>input,<>aux;
	
	*new{
		^super.new.initJAHInsertDict();
	}
	
	*initClass{
		
		fx = (\JAHTremolo:"Tremolo",\JAHEnvFilter:"Envelope Filter",\JAHFreeVerb:"FreeVerb");
		gen = (\JahTabletTgrain:"TGrains",\JahTabletSlice:"Tablet Slice");
		aux = (\JAHAuxInput:"Aux Input");
		input = (\JAHInput:"input");
	}
	
	
}