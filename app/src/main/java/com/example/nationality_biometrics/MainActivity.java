package com.example.nationality_biometrics;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    // Vistas principales
    private Button btnScanFingerprint;
    private TextView tvStatus, tvTime;
    private View formLayout; // Usamos View porque es un ConstraintLayout en el XML
    private TextInputEditText etNombre, etSexo, etEdad, etResidencia, etNacionalidad;
    private ProgressBar progressBar;

    // Cronómetro
    private long startTime;
    private Runnable timerRunnable;
    private android.os.Handler timerHandler = new android.os.Handler();
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de vistas
        initializeViews();

        // Configurar listeners
        setupListeners();
    }

    /**
     * Inicializa todas las vistas de la actividad.
     */
    private void initializeViews() {
        btnScanFingerprint = findViewById(R.id.btnScanFingerprint);
        tvStatus = findViewById(R.id.tvStatus);
        tvTime = findViewById(R.id.tvTime);
        formLayout = findViewById(R.id.formLayout);
        progressBar = findViewById(R.id.progressBar);

        // Inicializar campos del formulario
        etNombre = findViewById(R.id.etNombre);
        etSexo = findViewById(R.id.etSexo);
        etEdad = findViewById(R.id.etEdad);
        etResidencia = findViewById(R.id.etResidencia);
        etNacionalidad = findViewById(R.id.etNacionalidad);
    }

    /**
     * Configura los listeners para las vistas interactivas.
     */
    private void setupListeners() {
        btnScanFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFingerprint();
            }
        });
    }

    /**
     * Inicia el proceso de escaneo biométrico y muestra el loading.
     */
    private void scanFingerprint() {
        tvStatus.setText("Estado: Escaneando huella...");
        formLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        startTimer();

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopTimer();
                                progressBar.setVisibility(View.GONE);
                                tvStatus.setText("Estado: Autenticación exitosa");
                                simulateUserData();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                tvStatus.setText("Huella no reconocida. Intenta de nuevo.");
                                stopTimer();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopTimer();
                                progressBar.setVisibility(View.GONE);
                                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                    tvStatus.setText("Autenticación cancelada. Intenta de nuevo.");
                                } else {
                                    tvStatus.setText("Error de biometría: " + errString + ". Intenta de nuevo.");
                                }
                            }
                        });
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Escaneo de huella")
                .setSubtitle("Autentique para continuar")
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Inicia el cronómetro y actualiza el TextView de tiempo.
     */
    private void startTimer() {
        startTime = SystemClock.elapsedRealtime();
        isTimerRunning = true;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = SystemClock.elapsedRealtime() - startTime;
                int minutes = (int) (millis / 60000);
                int seconds = (int) ((millis / 1000) % 60);
                int ms = (int) (millis % 1000);
                // Mostrar tiempo en formato mm:ss.SSS mientras corre
                tvTime.setText(String.format("Tiempo: %02d:%02d.%03d", minutes, seconds, ms));
                if (isTimerRunning) {
                    timerHandler.postDelayed(this, 50); // Actualizar cada 50ms
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    /**
     * Detiene el cronómetro.
     */
    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    /**
     * Simula la obtención de datos de usuario por huella y rellena el formulario.
     * Excluye nacionalidades Guatemala y Estados Unidos.
     */
    private void simulateUserData() {
        // Datos de ejemplo (excluyendo Guatemala y EE.UU)
        String[] nombres = {"Juan Pérez", "Ana López", "Carlos Ruiz", "María Torres", "Pedro García"};
        String[] sexos = {"Masculino", "Femenino", "Masculino", "Femenino", "Masculino"};
        String[] edades = {"30", "25", "40", "35", "28"};
        String[] residencias = {"Madrid", "Barcelona", "Valencia", "Sevilla", "Bilbao"};
        String[] nacionalidades = {"Española", "Mexicana", "Argentina", "Colombiana", "Chilena"};

        int idx = (int) (Math.random() * nombres.length);

        etNombre.setText(nombres[idx]);
        etSexo.setText(sexos[idx]);
        etEdad.setText(edades[idx]);
        etResidencia.setText(residencias[idx]);
        etNacionalidad.setText(nacionalidades[idx]);

        formLayout.setVisibility(View.VISIBLE);

        // Mostrar tiempo total al finalizar
        long millis = SystemClock.elapsedRealtime() - startTime;
        int minutes = (int) (millis / 60000);
        int seconds = (int) ((millis / 1000) % 60);
        int ms = (int) (millis % 1000);
        tvTime.setText(String.format("Tiempo total: %02d:%02d.%03d", minutes, seconds, ms));

        if (validateForm()) {
            tvStatus.setText("Datos cargados correctamente");
        }
    }

    /**
     * Valida que todos los campos del formulario estén completos y correctos.
     * @return true si el formulario es válido, false en caso contrario.
     */
    private boolean validateForm() {
        String nombre = etNombre.getText().toString().trim();
        String sexo = etSexo.getText().toString().trim();
        String edadStr = etEdad.getText().toString().trim();
        String residencia = etResidencia.getText().toString().trim();
        String nacionalidad = etNacionalidad.getText().toString().trim();

        if (nombre.isEmpty() || sexo.isEmpty() || edadStr.isEmpty() ||
                residencia.isEmpty() || nacionalidad.isEmpty()) {
            tvStatus.setText("Por favor, completa todos los campos.");
            return false;
        }

        try {
            int edad = Integer.parseInt(edadStr);
            if (edad <= 0 || edad > 120) {
                tvStatus.setText("Edad inválida. Ingresa un valor numérico válido.");
                return false;
            }
        } catch (NumberFormatException e) {
            tvStatus.setText("Edad inválida. Ingresa un valor numérico.");
            return false;
        }

        // Excluir Guatemala y Estados Unidos
        if (nacionalidad.equalsIgnoreCase("Guatemala") ||
                nacionalidad.equalsIgnoreCase("Guatemalteca") ||
                nacionalidad.equalsIgnoreCase("Estados Unidos") ||
                nacionalidad.equalsIgnoreCase("Estadounidense") ||
                nacionalidad.equalsIgnoreCase("EE.UU") ||
                nacionalidad.equalsIgnoreCase("USA")) {
            tvStatus.setText("Nacionalidad no permitida.");
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar callbacks para evitar memory leaks
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}