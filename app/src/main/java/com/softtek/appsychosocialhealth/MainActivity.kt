package com.softtek.appsychosocialhealth

// GARANTA QUE ESTES IMPORTS ESTEJAM PRESENTES NO TOPO DO ARQUIVO
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class MainActivity : AppCompatActivity() {

    // Declaração dos componentes da UI
    private lateinit var spinnerMood: Spinner
    private lateinit var radioGroupWorkload: RadioGroup
    private lateinit var seekBarLeaderRelationship: SeekBar
    private lateinit var seekBarColleagueRelationship: SeekBar
    private lateinit var buttonSubmit: Button
    private lateinit var buttonViewResults: Button

    // Instância do Firebase Auth e Firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o Firebase Auth e o Firestore
        auth = Firebase.auth
        db = Firebase.firestore

        // Conecta os componentes da UI com seus IDs no XML
        spinnerMood = findViewById(R.id.spinnerMood)
        radioGroupWorkload = findViewById(R.id.radioGroupWorkload)
        seekBarLeaderRelationship = findViewById(R.id.seekBarLeaderRelationship)
        seekBarColleagueRelationship = findViewById(R.id.seekBarColleagueRelationship)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonViewResults = findViewById(R.id.buttonViewResults)

        // Configura o listener do botão de envio
        buttonSubmit.setOnClickListener {
            submitCheckinData()
        }

        // Listener para o botão de ver resultados (funcionalidade futura)
        buttonViewResults.setOnClickListener {
            Toast.makeText(this, "A tela de resultados será implementada em breve!", Toast.LENGTH_SHORT).show()
        }

        // Autentica o usuário anonimamente
        signInAnonymously()
    }

    private fun signInAnonymously() {
        // Verifica se o usuário já está logado para não fazer login toda vez que a activity é criada
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sucesso na autenticação
                        Log.d("Auth", "signInAnonymously:success")
                        val user = auth.currentUser
                        Toast.makeText(baseContext, "Usuário conectado.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Falha na autenticação
                        Log.w("Auth", "signInAnonymously:failure", task.exception)
                        Toast.makeText(baseContext, "Falha na autenticação.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun submitCheckinData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Erro: Usuário não autenticado. Tente novamente.", Toast.LENGTH_SHORT).show()
            signInAnonymously() // Tenta reconectar se o usuário for nulo
            return
        }

        // Coleta os dados dos componentes da UI
        val mood = spinnerMood.selectedItem.toString()

        val selectedWorkloadId = radioGroupWorkload.checkedRadioButtonId
        if (selectedWorkloadId == -1) {
            Toast.makeText(this, "Por favor, selecione sua carga de trabalho.", Toast.LENGTH_SHORT).show()
            return // Para a execução se nada for selecionado
        }
        val workloadRadioButton = findViewById<RadioButton>(selectedWorkloadId)
        val workload = workloadRadioButton.text.toString()

        // As SeekBars vão de 0 a 4, então somamos 1 para ter a escala de 1 a 5
        val leaderRelationship = seekBarLeaderRelationship.progress + 1
        val colleagueRelationship = seekBarColleagueRelationship.progress + 1

        // Cria um objeto (mapa) com os dados a serem salvos
        val checkinData = hashMapOf(
            "userId" to currentUser.uid,
            "timestamp" to Date(),
            "mood" to mood,
            "workload" to workload,
            "leaderRelationship" to leaderRelationship,
            "colleagueRelationship" to colleagueRelationship
        )

        // Salva os dados no Firestore em uma coleção chamada "checkins"
        db.collection("checkins")
            .add(checkinData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(this, "Respostas enviadas com sucesso!", Toast.LENGTH_LONG).show()
                resetForm() // Limpa o formulário após o envio
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
                Toast.makeText(this, "Erro ao enviar respostas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun resetForm() {
        spinnerMood.setSelection(0)
        radioGroupWorkload.clearCheck()
        seekBarLeaderRelationship.progress = 2 // Valor padrão (meio)
        seekBarColleagueRelationship.progress = 2 // Valor padrão (meio)
    }
}

