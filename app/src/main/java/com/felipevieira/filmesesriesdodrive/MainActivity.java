package com.felipevieira.filmesesriesdodrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.sax.TextElementListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    LinearLayout lay_out; //Onde será adicionado os botões, etc
    EditText txt_pesquisa;

    ImageButton btn_busca;
    List<String> filmes_listados = new ArrayList<String>();
    List<String> filmes_encontrados = new ArrayList<String>();

    List<String> series_listadas = new ArrayList<String>();
    List<String> series_encontradas = new ArrayList<String>();

    List<String> temporadas = new ArrayList<String>();
    List<String> episodios = new ArrayList<String>();
    List<String> link_episodios = new ArrayList<String>();

    String link_fb = "",link_at = "";
    int resultados = 0;
    Boolean pesquisou = false;
    int contador = 0;
    final FirebaseDatabase database = FirebaseDatabase.getInstance(); //Instancia do Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lay_out = findViewById(R.id.layoutMestre);
        btn_busca = (ImageButton)findViewById(R.id.btnPesquisa);
        txt_pesquisa = (EditText) findViewById(R.id.txtPesquisa);
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Aviso!");
        alert.setMessage("Se uma tela vermelha aparecer dizendo que não existe vídeo disponível:\n" +
                "1 - O filme pode ter sido denunciado e ter saído do ar.\n" +
                "2 - O limite de exibições do Google Drive foi excedido, tente novamente mais tarde.");
        alert.setPositiveButton("OK",null);
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                buscarTUDO();
            }
        });
        alert.show();
        txt_pesquisa.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    btn_busca.performClick();
                }
                return false;
            }
        });
        btn_busca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contador == 1 && txt_pesquisa.getText().toString().equals("") ) {
                    lay_out.removeAllViews();
                    filmes_listados.clear();
                    series_listadas.clear();
                    buscarTUDO();
                }
                else{
                    resultados = 0;
                    filmes_encontrados.clear();
                    series_encontradas.clear();
                    String busca = txt_pesquisa.getText().toString();
                    int qtd_filmes = filmes_listados.size();
                    int qtd_series = series_listadas.size();
                    for (int a = 0; a < qtd_filmes; a++) {
                        if (filmes_listados.get(a).toUpperCase().contains(busca.toUpperCase())) {
                            filmes_encontrados.add(filmes_listados.get(a));
                        }
                    }
                    for (int b = 0; b < qtd_series; b++) {
                        if (series_listadas.get(b).toUpperCase().contains(busca.toUpperCase())) {
                            series_encontradas.add(series_listadas.get(b));
                        }
                    }
                    resultados = filmes_encontrados.size()+series_encontradas.size();
                    if (resultados == 0) {
                        MostraAviso("Nenhuma correspondência foi encontrada :/");
                    }
                    else {
                        if (txt_pesquisa.getText().toString().length() != 0 && contador == 0) {
                            contador = 1;
                            MostraAviso("Para listar todo o conteúdo novamente, faça uma pesquisa vazia.");
                        }
                        txt_pesquisa.setText("");
                        lay_out.removeAllViews();
                        toastRapido("Listando...", false);
                        pesquisou = true;
                        //MostraAviso(resultados+"");
                        for (int x = 0; x < filmes_encontrados.size(); x++) {
                            database.getReference("Filme/" + filmes_encontrados.get(x)).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String nome_filme = dataSnapshot.getKey().toString();
                                    final Button btn = new Button(getApplicationContext());
                                    btn.setText(nome_filme);
                                    btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    String caminho_da_imagem = "Filme/" + nome_filme + "/Imagem";
                                    DatabaseReference pegaImagem = database.getReference(caminho_da_imagem);
                                    pegaImagem.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                ImageView foto_filme = new ImageView(getApplicationContext());
                                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(700, 300);
                                                foto_filme.setLayoutParams(layoutParams);
                                                lay_out.addView(foto_filme);
                                                lay_out.addView(btn);
                                                String cod_image = dataSnapshot.getValue().toString();
                                                Picasso.get().load(cod_image).into(foto_filme);
                                            }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getApplicationContext(), "Imagens não serão baixadas.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String caminho = "Filme/" + btn.getText().toString() + "/Link";
                                    DatabaseReference nova = database.getReference(caminho);
                                    nova.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Intent i = new Intent(MainActivity.this, WebPlayer.class);
                                            i.putExtra("URL_FILME", dataSnapshot.getValue().toString());
                                            startActivity(i);
                                        }

                                        @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getApplicationContext(), "Cancelado.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        }
                            @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                    for (int x = 0; x < series_encontradas.size(); x++) {
                        database.getReference("Série/" + series_encontradas.get(x)).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                final String nome_serie = dataSnapshot.getKey();
                                final Button b_s = new Button(getApplicationContext());
                                b_s.setText(nome_serie);
                                b_s.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                String caminho_da_imagem = "Série/"+nome_serie+"/Imagem";
                                DatabaseReference pegaImagem = database.getReference(caminho_da_imagem);
                                pegaImagem.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        ImageView foto_filme = new ImageView(getApplicationContext());
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(700, 300);
                                        foto_filme.setLayoutParams(layoutParams);
                                        lay_out.addView(foto_filme);
                                        lay_out.addView(b_s);
                                        String cod_image = dataSnapshot.getValue().toString();
                                        Picasso.get().load(cod_image).into(foto_filme);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        //Se for cancelado a busca por imagens....
                                        Toast.makeText(getApplicationContext(),"Imagens não serão baixadas.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                b_s.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            temporadas.clear();
                                            database.getReference("Série/"+nome_serie+"/Temporadas").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot sn: dataSnapshot.getChildren()) {
                                                    String val_t = sn.getKey();
                                                    temporadas.add(val_t);
                                                }
                                                final String[] texto = new String[temporadas.size()];
                                                temporadas.toArray(texto);
                                                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                                alert.setTitle("Temporadas");
                                                alert.setItems(texto, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        episodios.clear();
                                                        link_episodios.clear();
                                                        database.getReference("Série/"+nome_serie+"/Temporadas/"+texto[which]).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                for(DataSnapshot g : dataSnapshot.getChildren()){
                                                                    String nome_ep = g.getKey();
                                                                    episodios.add(nome_ep);
                                                                    link_episodios.add(g.getValue().toString());
                                                                }
                                                                final String[] texto2 = new String[episodios.size()];
                                                                episodios.toArray(texto2);

                                                                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                                                alert.setTitle("Episódios");
                                                                alert.setItems(texto2, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        Intent i = new Intent(MainActivity.this, WebPlayer.class);
                                                                        i.putExtra("URL_FILME",link_episodios.get(which).toString());
                                                                        startActivity(i);
                                                                    }
                                                                });
                                                                alert.show();
                                                            }
                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            }
                                                        });
                                                    }
                                                });
                                                alert.show();
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                        resultados = 0;
                    }
                }
            }
        });
    }

        public  void buscarTUDO(){
        final DatabaseReference myRef = database.getReference("Filme");
        Toast.makeText(getApplicationContext(),"Buscando...",Toast.LENGTH_LONG).show();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String nome_filme = snapshot.getKey().toString();
                        filmes_listados.add(nome_filme);
                        final Button btn = new Button(getApplicationContext());
                        btn.setText(nome_filme);
                        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        String caminho_da_imagem = "Filme/"+nome_filme+"/Imagem";
                        DatabaseReference pegaImagem = database.getReference(caminho_da_imagem);
                        pegaImagem.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ImageView foto_filme = new ImageView(getApplicationContext());
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(700, 300);
                                foto_filme.setLayoutParams(layoutParams);
                                lay_out.addView(foto_filme);
                                lay_out.addView(btn);
                                String cod_image = dataSnapshot.getValue().toString();
                                Picasso.get().load(cod_image).into(foto_filme);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(),"Imagens não serão baixadas.",Toast.LENGTH_SHORT).show();
                            }
                        });

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String caminho = "Filme/"+btn.getText().toString()+"/Link";
                                DatabaseReference nova = database.getReference(caminho);
                                nova.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Intent i = new Intent(MainActivity.this, WebPlayer.class);
                                            i.putExtra("URL_FILME", dataSnapshot.getValue().toString());
                                            startActivity(i);
                                        }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(getApplicationContext(),"Cancelado.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
                @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),"Cancelado.",Toast.LENGTH_SHORT).show();
                    }
            });
            database.getReference("/Série").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    for(DataSnapshot dt: dataSnapshot.getChildren()){
                        final String nome_serie = dt.getKey();
                        series_listadas.add(nome_serie);
                        final Button b_s = new Button(getApplicationContext());
                        b_s.setText(nome_serie);
                        b_s.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        String caminho_da_imagem = "Série/"+nome_serie+"/Imagem";
                        DatabaseReference pegaImagem = database.getReference(caminho_da_imagem);
                        pegaImagem.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ImageView foto_filme = new ImageView(getApplicationContext());
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(700, 300);
                                    foto_filme.setLayoutParams(layoutParams);
                                    lay_out.addView(foto_filme);
                                    lay_out.addView(b_s);
                                    String cod_image = dataSnapshot.getValue().toString();
                                    Picasso.get().load(cod_image).into(foto_filme);
                                }

                            @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(getApplicationContext(),"Imagens não serão baixadas.",Toast.LENGTH_SHORT).show();
                                }
                        });
                        b_s.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            temporadas.clear();
                            database.getReference("Série/"+nome_serie+"/Temporadas").addValueEventListener(new ValueEventListener() {
                                @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot sn: dataSnapshot.getChildren()) {
                                            String val_t = sn.getKey();
                                            temporadas.add(val_t);
                                        }
                                        final String[] texto = new String[temporadas.size()];
                                        temporadas.toArray(texto);
                                        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                        alert.setTitle("Temporadas");
                                        alert.setItems(texto, new DialogInterface.OnClickListener() {
                                            @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                //toastRapido(texto[which],false);
                                                episodios.clear();
                                                link_episodios.clear();
                                                database.getReference("Série/"+nome_serie+"/Temporadas/"+texto[which]).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot g : dataSnapshot.getChildren()){
                                                            String nome_ep = g.getKey();
                                                            episodios.add(nome_ep);
                                                            link_episodios.add(g.getValue().toString());
                                                        }
                                                        final String[] texto2 = new String[episodios.size()];
                                                        episodios.toArray(texto2);
                                                        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                                        alert.setTitle("Episódios");
                                                        alert.setItems(texto2, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent i = new Intent(MainActivity.this, WebPlayer.class);
                                                                i.putExtra("URL_FILME",link_episodios.get(which).toString());
                                                                startActivity(i);
                                                            }
                                                        });
                                                        alert.show();
                                                    }
                                                @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    }
                                                });
                                            }
                                        });
                                        alert.show();
                                }
                                    @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                            });
                        }
                        });
                    }
                }
                @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
            });
    }
    public void MostraAviso(String data){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Aviso!");
        alert.setMessage(data);
        alert.setPositiveButton("OK",null);
        alert.show();
    }
    public void  toastRapido(String texto, Boolean curto){
        if(curto){
            Toast.makeText(getApplicationContext(),texto,Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),texto,Toast.LENGTH_LONG).show();
        }
    }
}