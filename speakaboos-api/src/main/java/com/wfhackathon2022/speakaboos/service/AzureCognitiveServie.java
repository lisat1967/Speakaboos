package com.wfhackathon2022.speakaboos.service;


import java.math.BigDecimal;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioOutputStream;
import com.wfhackathon2022.speakaboos.exception.PronunciationException;
import com.wfhackathon2022.speakaboos.model.SpeechLanguage;
import com.wfhackathon2022.speakaboos.util.PushAudioOutputStreamSampleCallback;

@Service
public class AzureCognitiveServie {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AzureCognitiveServie.class);

    // Replace below with your own subscription key
    private static final String speechSubscriptionKey = "8bbe96c8331e4f31896a8cc435bbe355";
    // Replace below with your own service region (e.g., "westus").
    private static final String serviceRegion = "eastus";
    
    private static final String ssml =   "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"en-US\">"
    		                    + "<voice name=\"$varvoice$\">"
    		                    + "<prosody rate=\"$varrate$\">"
    		                    + "$vartext$"
    		                    + "</prosody>"
    		                    + "</voice>"
    		                    + "</speak>";
    
    
	public byte[] retrieveSpeech(String text, String language, BigDecimal speed) {
		byte[] audio = null;        
        SpeechConfig config = null;
        SpeechSynthesizer synth = null;
        AudioConfig streamConfig = null;
        
		try {
			config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
			
	        // Sets the synthesis output format.
	        config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);
	        
	        // Creates an instance of a customer class inherited from PushAudioOutputStreamCallback
	        PushAudioOutputStreamSampleCallback callback = new PushAudioOutputStreamSampleCallback();

	        // Creates an audio out stream from the callback.
	        PushAudioOutputStream stream = AudioOutputStream.createPushStream(callback);

	        // Creates a speech synthesizer using audio stream output.
	        streamConfig = AudioConfig.fromStreamOutput(stream);
	        
			synth = new SpeechSynthesizer(config, streamConfig);
			
			//Set the language
			config.setSpeechSynthesisLanguage(language);
			String voice = SpeechLanguage.getVoice(language);	        
			String textWithRateSsml = ssml.replace("$varrate$", speed.toString()).replace("$vartext$", text).replace("$varvoice$", voice);
			LOG.info("textWithRateSsml::"+textWithRateSsml);
			SpeechSynthesisResult result = synth.SpeakSsml(textWithRateSsml);

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
            	LOG.info("AzureCognitiveServie::retrieveSpeech::completed successfully for "+ text + " in "+language);
            	audio = callback.getAudioData();
            }
            else if (result.getReason() == ResultReason.Canceled) {
            	//CODE_DEBT: Error Handle it
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
                LOG.error("AzureCognitiveServie::retrieveSpeech:: "+ text +" CANCELED: Reason=" + cancellation.getReason());
                
                if (cancellation.getReason() == CancellationReason.Error) {
                	LOG.error("AzureCognitiveServie::retrieveSpeech:: "+ text +"CANCELED: ErrorCode=" + cancellation.getErrorCode());
                	LOG.error("AzureCognitiveServie::retrieveSpeech:: "+ text +"CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                }
                throw new PronunciationException(cancellation.getReason().toString(), "WFH9002");
            }

        } catch (Exception ex) {
            LOG.error("AzureCognitiveServie::retrieveSpeech::exception occured: "+ex.getMessage(), ex);
            throw new PronunciationException(ex.getMessage(), "WFH9002", ex);

        }
		finally {
			if(synth != null) {
				synth.close();
			}
			if(streamConfig != null) {
				streamConfig.close();
			}
		}
		return audio;
	}

}