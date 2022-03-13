package com.audiopixel.audiopixeldungeon.desktop;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;


public class TTSManager{


    public  VoiceManager freettsVM = VoiceManager.getInstance();
    public  Voice freeVoice;



    public TTSManager(){
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        freeVoice = freettsVM.getVoice("kevin16");
        freeVoice.allocate();
        freeVoice.setRate(140);//Setting the rate of the voice
        freeVoice.setPitch(150);//Setting the Pitch of the voice
        freeVoice.setVolume(3);//Setting the volume of the voice
    }

    public void speakMsg(String msg){
        Thread thread = new Thread(new msgRunnable(msg));
        thread.start();
    }



    class msgRunnable implements Runnable{
        String msg;
        msgRunnable(String txt){
            this.msg = txt;
        }
        @Override
        public void run() {
            freeVoice.speak(msg);
        }
    }

}

