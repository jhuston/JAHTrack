JAHAudience{
	
	var <>group,<>inbus,<>outbus,<>buf;
	var <>recButt,<>playButt,<>inputList;
	var <>win;
	var <>recSynth;
	var <>playSynth;
	var <>s;
	*new{|group,outbus,inbus|
		^super.new.initJAHAudience(group,outbus,inbus);
		}
		
	initJAHAudience{|arggroup,argoutbus,arginbus|
		s = Server.default;
		SynthDef(\JAHAudienceRecord,{|bufnum,inbus,run = 1|
			var input;
			input = SoundIn.ar(inbus);
			RecordBuf.ar(input,bufnum,run:run,loop:1);
		}).load(s);
		SynthDef(\JAHAudiencePlay,{|bufnum,outbus|
			Out.ar(outbus, PlayBuf.ar(1, bufnum, loop:1));
		}).load(s);
		
		buf = Buffer.alloc(s,s.sampleRate * 5,1);
		group = arggroup;
		inbus = arginbus;
		outbus = argoutbus;
		if(JAH.name != "JAH"){JAH.new};
		JAH.globalBufferDict.put(('Audience'++buf.bufnum).asSymbol,buf);
		JAH.sendBufList;
		
	}
	gui{
		win = SCWindow("AudienceRec",Rect(100,100,250,40)).front;
		win.onClose = {this.freeAll};
		win.view.decorator = FlowLayout.new(win.bounds);
		recButt = RoundButton(win,Rect(0,0,30,30))
				.states_([[\record,Color.black,Color.white],[\record,Color.red,Color.white]])
				.canFocus_(false)
				.font_(Font("Helvetica",10))
				.border_(1)
				.extrude_(false)
				.action_({|butt|
					if(butt.value == 1){
						this.startRec();
					}{
						this.stopRec();
					};
				});
		recButt = RoundButton(win,Rect(0,0,30,30))
				.states_([[\play,Color.black,Color.white],[\play,Color.red,Color.white]])
				.canFocus_(false)
				.font_(Font("Helvetica",10))
				.border_(1)
				.extrude_(false)
				.action_({|butt|
					if(butt.value == 1){
						this.startPlay();
					}{
						this.stopPlay();
					};
				});
		inputList = SCPopUpMenu(win,Rect(0,0,40,20))
				.items_([\0,\1,\2,\3,\4,\5,\6,\7])
				.action_({|menu|
					inbus = menu.value;
				});
	}
	startRec{
		recSynth = Synth(\JAHAudienceRecord,[\inbus,inbus,\bufnum,buf]);
	}
	stopRec{
		recSynth.free;
	}
	startPlay{
		playSynth = Synth(\JAHAudiencePlay,[\outbus,outbus,\bufnum,buf]);
	}
	stopPlay{
		playSynth.free;
	}
	freeAll{
		recSynth.free;
		playSynth.free;
		buf.free;
	}
}