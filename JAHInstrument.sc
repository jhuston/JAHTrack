JAHAbstractInstrument{
	classvar <>instrumentDictionary,<>s;
	*initClass{
		instrumentDictionary = ();
		s = Server.default;
		this.allSubclasses.do{|class|
			instrumentDictionary.put(class.name,class);
		};
	}
}
JAHInstrument{
	
	var <>inbus,<>outbus,<>inputGroup;
	var <>listView;
	var <>track;
	var <>powerButt;
	var <>parentView;
	var <>inputArray,<>itemBase;
	var <>instrument;
	
	*new{|parent,outbus,inputGroup|
		^super.new.initJAHInstrument(parent,outbus,inputGroup);
	}
	
	initJAHInstrument{|argparent,argoutbus,argGroup|

		track = argparent;
		parentView = track.trackView;
		outbus = argoutbus;
		inputGroup = argGroup;
		itemBase = [\NONE,"-"];
		inputArray = JAHInsertDict.gen.keys.asArray.flat;
		listView = PopUpMenu(parentView,Rect(0,0,70,15))
					.background_(Color.grey)
					.font_(Font("Helvetica",9))
					.canFocus_(false)
					.items_(itemBase++inputArray)
					.action_({|menu|
						var val;
						if(menu.item != \NONE){
							val = listView.item;
							instrument = JAHAbstractInstrument.instrumentDictionary.at(val).new(inputGroup,outbus);
							if(instrument.class.findMethod('gui')!= nil){
								instrument.gui;
							}
						}{
							this.removeInstrument();
						};
					});
	}
}