JAHInsert{
	
	var parent;
	var settingsDict;
	var parentView;
	var <>listView,<>enableButt,<>powerButt;
	var <>insertArray;
	var <>track;
	var <>id;
	var <>fx;
	var <>insertGroup;
	var <type;
	var <>currentItem;
	
	*new{|parent,type|
		^super.new.initJAHInsert(parent,type);
	}
	
	initJAHInsert{|argparent,argtype|
		settingsDict = ();
		currentItem = 0;
		track = argparent;
		type = argtype ? \insert;
		parentView = track.trackView;
		switch(type)
			{\insert}
				{insertArray = JAHInsertDict.fx.keys.asArray.flat;}
			{\gen}
				{insertArray = JAHInsertDict.gen.keys.asArray.flat;}
			{\input}
				{insertArray = JAHInsertDict.input.keys.asArray.flat;}
			{\aux}
				{insertArray = JAHInsertDict.aux.keys.asArray.flat;};
				
		listView = SCPopUpMenu(parentView,Rect(0,0,80,15))
				.background_(Color.grey)
				.canFocus_(false)
				.items_(["NONE","-"]++insertArray)
				.action_({|menu|
					var val;
					currentItem = menu.value;
					if(menu.item != "NONE"){
						try{
							this.fx.remove();
						};
						val = listView.item++().asCompileString;
						fx = val.interpret;
						fx.target_(insertGroup);
						fx.parentInsert_(this);
						switch(type)
						{\insert}
							{fx.inbus_(track.internalBus)}
						{\input}
							{fx.inbus_(track.inbus);}
						{\aux}
						{fx.inbus_(JAHDesk.auxBusDict.at(track.trackName.asSymbol));};
						fx.outbus_(track.internalBus);
						fx.setSynthArgs;
						fx.postln;
					}{
						this.removeFx();
					};
					})
				.font_(Font("Helvetica",9));
				
		powerButt = RoundButton(parentView,Rect(0,0,15,15))
				.states_([[\power,Color.black,Color.white],[\power,Color.red,Color.white]])
				.canFocus_(false)
				.extrude_(false)
				.border_(1)
				.action_({|butt|
					butt.value.postln;
					if(butt.value == 1){
						this.fx.on();
					}{
						this.fx.off();
					};
				})
				.mouseDownAction_({|butt|
					powerButt.valueAction_(butt.value);
				});
		enableButt = RoundButton(parentView,Rect(0,0,15,15))
				.states_([[\search,Color.black,Color.white],[\search,Color.red,Color.white]])
				.canFocus_(false)
				.extrude_(false)
				.border_(1)
				.action_({|butt|
					butt.value.postln;
					if(butt.value == 1){
						this.open();
					}{
						this.close();
					};
				})
				.mouseDownAction_({|butt|
					enableButt.valueAction_(butt.value);
				});
		
		
	}
	
	open{
		"enabled".postln;
		if(fx.notNil){
			fx.gui;	
		}
	}
	
	removeFx{
		this.enableButt.value_(0);
		this.fx.synth.free;
		this.fx.remove();
		this.fx = nil;
	}
	
	saveSettings{
		settingsDict.put(\insertSettings,[
			currentItem,enableButt.value,powerButt.value]);
			^settingsDict;
	}
	loadSettings{|argSettings|
		settingsDict = argSettings;
		listView.valueAction_(settingsDict.at(\insertSettings)[0]);
		enableButt.valueAction_(settingsDict.at(\insertSettings)[1]);
		powerButt.valueAction_(settingsDict.at(\insertSettings)[2]);
	}
	
	close{
		"close".postln;
			try{this.fx.hideGui;};
	}
}