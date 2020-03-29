package com.felipevieira.filmesesriesdodrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.sax.TextElementListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    final FirebaseDatabase database = FirebaseDatabase.getInstance(); //Instancia do Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }

        public  void buscarTUDO(){
            //Buscando pelos filmes
            final DatabaseReference myRef = database.getReference("Filme");
            //Avisa que está buscando no banco de dados
            Toast.makeText(getApplicationContext(),"Carregando...",Toast.LENGTH_LONG).show();

            //Cria o layout onde será adicionado os itens
            final LinearLayout lay_out = (LinearLayout) findViewById(R.id.layoutMestre);

            //Quando os filmes tem uma alteração
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Remove todos os itens para evitar bug
                    lay_out.removeAllViews();
                    //Percorre o banco buscando filmes
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //Pega o nome do filme
                        String nome_filme = snapshot.getKey().toString();

                        //Botões
                        final Button btn = new Button(getApplicationContext());
                        //Coloca o nome do filme no botão
                        btn.setText(nome_filme);
                        //Configura o botão para caber na tela
                        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        //Onde buscar as imagens
                        String caminho_da_imagem = "Filme/"+nome_filme+"/Imagem";
                        //Referência para buscar a imagem
                        DatabaseReference pegaImagem = database.getReference(caminho_da_imagem);

                        //Busca a imagem
                        pegaImagem.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //Onde será mostrado
                                ImageView foto_filme = new ImageView(getApplicationContext());
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(700, 300);
                                foto_filme.setLayoutParams(layoutParams);
                                //Adiciona o campo de imagem
                                lay_out.addView(foto_filme);
                                //Adiciona o botão
                                lay_out.addView(btn);

                                //Busca o link da imagem
                                String cod_image = dataSnapshot.getValue().toString();
                                //Baixa a imagem e aplica
                                Picasso.with(getApplicationContext()).load(cod_image).into(foto_filme);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                //Se for cancelado a busca por imagens....
                                Toast.makeText(getApplicationContext(),"Imagens não serão baixadas.",Toast.LENGTH_SHORT).show();
                            }
                        });

                        //Clique no botão
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Caminho onde estão os links dos arquivos de video "filmes"
                                String caminho = "Filme/"+btn.getText().toString()+"/Link";
                                DatabaseReference nova = database.getReference(caminho);
                                //LINK
                                nova.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //Abre o link em outra página
                                        Intent i = new Intent(MainActivity.this, WebPlayer.class);
                                        i.putExtra("URL_FILME", dataSnapshot.getValue().toString());
                                        startActivity(i);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        //Não está mais buscando o link
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
        }
        public void MostraAviso(String data){
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Aviso!");
            alert.setMessage(data);
            alert.setPositiveButton("OK",null);
            alert.show();
        }
    }

