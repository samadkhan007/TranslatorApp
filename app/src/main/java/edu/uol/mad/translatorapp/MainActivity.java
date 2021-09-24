package edu.uol.mad.translatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner,toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV;
    private MaterialButton translateBtn ,speechBtn;
    private EditText translatedTV;
    private Button shareBtn,copyBtn;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference root = db.getReference().child("History");

    TextToSpeech textToSpeech;


    String [] fromLanguages ={"From","English","Afrikaans","Arabic","Belarusian","German","Spanish","Catalan","French","Czech","Welsh","Hindi","Urdu"};

    String [] toLanguages ={"To","English","Afrikaans","Arabic","Belarusian","German","Spanish","Catalan","French","Czech","Welsh","Hindi","Urdu"};


    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode,fromLanguageCode,toLanguageCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fromSpinner =findViewById(R.id.idFromSpinner);
        toSpinner  =findViewById(R.id.idToSpinner);
        sourceEdt =findViewById(R.id.EditSource);
        micIV =findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        speechBtn = findViewById(R.id.idBtnSpeech);
        translatedTV = findViewById(R.id.idTvTranslatedTv);
        copyBtn =findViewById(R.id.copy);



      textToSpeech = new TextToSpeech(getApplicationContext(),
              new TextToSpeech.OnInitListener() {
                  @Override
                  public void onInit(int i) {
                      if (i == TextToSpeech.SUCCESS)
                      {
                          int lang= textToSpeech.setLanguage(Locale.getDefault());
                      }
                  }
              });



      speechBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              String s =translatedTV.getText().toString();
              int speech = textToSpeech.speak(s,TextToSpeech.QUEUE_FLUSH,null);
          }
      });



        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                fromLanguageCode = getLanguageCode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter fromadapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromadapter);


        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toadapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
        toadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toadapter);



        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Enter your text to translate ", Toast.LENGTH_SHORT).show();
                }
                else if (fromLanguageCode == 0)
                {
                    Toast.makeText(MainActivity.this, "Select Source langauge", Toast.LENGTH_SHORT).show();
                }
                else if (toLanguageCode == 0)
                {
                    Toast.makeText(MainActivity.this, "Select the langauge to make translation", Toast.LENGTH_SHORT).show();
                }
                else
                {
                   translateText(fromLanguageCode,toLanguageCode,sourceEdt.getText().toString());


                }
            }
        });



        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id ==R.id.share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, translatedTV.getText().toString());
            intent.putExtra(Intent.EXTRA_TEXT, " ");
            startActivity(Intent.createChooser(intent, "Share Via"));
            return super.onOptionsItemSelected(item);
        }
        if (id ==R.id.copy) {
           ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
           ClipData clip = ClipData.newPlainText("Edittext",translatedTV.getText().toString());
           clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
        if (id ==R.id.history) {
           Intent intent = new Intent(MainActivity.this,History.class);
           startActivity(intent);
            return super.onOptionsItemSelected(item);
        }
        return true;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE)
        {
            if (resultCode == RESULT_OK && data!= null)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }

    private void translateText(int fromLanguageCode , int toLanguageCode , String source)
    {
         translatedTV.setText("Downloading Model....");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                        String data = translatedTV.getText().toString();
                        String data1 = sourceEdt.getText().toString();
                        HashMap<String ,String> historyMap = new HashMap<>();
                        historyMap.put("From",data1);
                        historyMap.put("To:",data);
                        root.push().setValue(historyMap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to Translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "fail to download langauge model"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language)
        {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;

            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;

            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;

            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;

            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                break;

            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;

            case "Catalan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;

            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;

            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;

            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;

            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;

            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            default:
                languageCode =0;

        }
        return languageCode;

    }
}