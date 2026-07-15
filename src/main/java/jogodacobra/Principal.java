package snakegamefx;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class Principal extends Application {

    private static final int TAMANHO_BLOCO = 25;
    private int larguraTabuleiro; 
    private int alturaTabuleiro;  

    private enum Direcao { CIMA, BAIXO, ESQUERDA, DIREITA }
    private Direcao direcaoAtual = Direcao.DIREITA;

    private final ArrayList<int[]> cobra = new ArrayList<>();
    private final int[] comida = new int[2];
    private final Random random = new Random();
    private boolean gameOver = false;
    private boolean emMenu = true; 
    private int pontuacao = 0;

    private long intervaloUpdate = 120_000_000; 
    private String dificuldadeAtual = "Médio";

    @Override
    public void start(Stage primaryStage) {
        Rectangle2D limitesTela = Screen.getPrimary().getBounds();
        double larguraPixels = limitesTela.getWidth();
        double alturaPixels = limitesTela.getHeight();

        larguraTabuleiro = (int) (larguraPixels / TAMANHO_BLOCO);
        alturaTabuleiro = (int) (alturaPixels / TAMANHO_BLOCO);

        reiniciarJogo();

        Canvas canvas = new Canvas(larguraPixels, alturaPixels);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, larguraPixels, alturaPixels);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.ESCAPE) {
                primaryStage.close();
            }

            if (emMenu) {
                if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
                    intervaloUpdate = 180_000_000; 
                    dificuldadeAtual = "Fácil";
                    emMenu = false;
                    reiniciarJogo();
                } else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
                    intervaloUpdate = 110_000_000; 
                    dificuldadeAtual = "Médio";
                    emMenu = false;
                    reiniciarJogo();
                } else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                    intervaloUpdate = 65_000_000;  
                    dificuldadeAtual = "Difícil";
                    emMenu = false;
                    reiniciarJogo();
                }
            } else {
                if ((code == KeyCode.UP || code == KeyCode.W) && direcaoAtual != Direcao.BAIXO) {
                    direcaoAtual = Direcao.CIMA;
                } else if ((code == KeyCode.DOWN || code == KeyCode.S) && direcaoAtual != Direcao.CIMA) {
                    direcaoAtual = Direcao.BAIXO;
                } else if ((code == KeyCode.LEFT || code == KeyCode.A) && direcaoAtual != Direcao.DIREITA) {
                    direcaoAtual = Direcao.ESQUERDA;
                } else if ((code == KeyCode.RIGHT || code == KeyCode.D) && direcaoAtual != Direcao.ESQUERDA) {
                    direcaoAtual = Direcao.DIREITA;
                } else if (gameOver) {
                    if (code == KeyCode.R) {
                        reiniciarJogo(); 
                    } else if (code == KeyCode.M) {
                        emMenu = true;   
                        gameOver = false;
                    }
                }
            }
        });

        primaryStage.setTitle("Jogo da Cobra - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true); 
        primaryStage.setFullScreenExitHint("Pressione ESC para sair do jogo"); 
        primaryStage.show();

        new AnimationTimer() {
            private long ultimoUpdate = 0;

            @Override
            public void handle(long agora) {
                if (agora - ultimoUpdate >= (emMenu ? 100_000_000 : intervaloUpdate)) {
                    if (!gameOver && !emMenu) {
                        atualizarLogica();
                    }
                    desenhar(gc, larguraPixels, alturaPixels);
                    ultimoUpdate = agora;
                }
            }
        }.start();
    }

    private void atualizarLogica() {
        int[] cabecaAtual = cobra.get(0);
        int novaX = cabecaAtual[0];
        int novaY = cabecaAtual[1];

        switch (direcaoAtual) {
            case CIMA:    novaY--; break;
            case BAIXO:   novaY++; break;
            case ESQUERDA:novaX--; break;
            case DIREITA: novaX++; break;
        }

        if (novaX < 0 || novaX >= larguraTabuleiro || novaY < 0 || novaY >= alturaTabuleiro) {
            gameOver = true;
            return;
        }

        for (int[] parte : cobra) {
            if (parte[0] == novaX && parte[1] == novaY) {
                gameOver = true;
                return;
            }
        }

        cobra.add(0, new int[]{novaX, novaY});

        if (novaX == comida[0] && novaY == comida[1]) {
            pontuacao += 10;
            gerarComida();
        } else {
            cobra.remove(cobra.size() - 1);
        }
    }

    private void desenhar(GraphicsContext gc, double larguraPixels, double alturaPixels) {
        // Fundo estilo gramado escuro de desenho
        gc.setFill(Color.web("#1e272c"));
        gc.fillRect(0, 0, larguraPixels, alturaPixels);

        if (emMenu) {
            gc.setFill(Color.web("#2ecc71"));
            gc.setFont(new Font("Arial Bold", 46));
            gc.fillText("JOGO DA COBRA 2D", larguraPixels / 2.0 - 220, alturaPixels / 2.0 - 150);

            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 24));
            gc.fillText("Escolha a Dificuldade:", larguraPixels / 2.0 - 120, alturaPixels / 2.0 - 50);

            gc.setFill(Color.web("#2ecc71"));
            gc.fillText("[ 1 ] - Fácil", larguraPixels / 2.0 - 60, alturaPixels / 2.0 + 10);
            gc.setFill(Color.web("#f1c40f"));
            gc.fillText("[ 2 ] - Médio", larguraPixels / 2.0 - 60, alturaPixels / 2.0 + 60);
            gc.setFill(Color.web("#e74c3c"));
            gc.fillText("[ 3 ] - Difícil", larguraPixels / 2.0 - 60, alturaPixels / 2.0 + 110);

            gc.setFill(Color.GRAY);
            gc.setFont(new Font("Arial Italic", 16));
            gc.fillText("Use as teclas numéricas | Pressione ESC para sair", larguraPixels / 2.0 - 180, alturaPixels / 2.0 + 200);
            return;
        }

        // --- DESENHAR O OVO (COMIDA) ---
        double comidaX = comida[0] * TAMANHO_BLOCO;
        double comidaY = comida[1] * TAMANHO_BLOCO;

        // Casca do ovo (Formato oval branco com sombreado suave na borda)
        gc.setFill(Color.web("#f5f6fa"));
        gc.fillOval(comidaX + 2, comidaY + 1, TAMANHO_BLOCO - 4, TAMANHO_BLOCO - 2);
        
        // Gema do ovo (Círculo laranja centralizado e brilhante)
        gc.setFill(Color.web("#f39c12"));
        gc.fillOval(comidaX + 7, comidaY + 7, TAMANHO_BLOCO - 14, TAMANHO_BLOCO - 14);
        // Brilho na gema (ponto branco pequeno)
        gc.setFill(Color.WHITE);
        gc.fillOval(comidaX + 9, comidaY + 9, 3, 3);

        // --- DESENHAR A COBRA ESTILO DESENHO ---
        for (int i = 0; i < cobra.size(); i++) {
            int[] parte = cobra.get(i);
            double x = parte[0] * TAMANHO_BLOCO;
            double y = parte[1] * TAMANHO_BLOCO;

            if (i == 0) {
                // CABEÇA DA COBRA (Verde mais escuro arredondado)
                gc.setFill(Color.web("#27ae60"));
                gc.fillOval(x, y, TAMANHO_BLOCO, TAMANHO_BLOCO);

                // Desenhar Olhos de Desenho Animado
                gc.setFill(Color.WHITE);
                double olhoTamanho = 7;
                double olhoEsquerdoX = x + 4;
                double olhoEsquerdoY = y + 4;
                double olhoDireitoX = x + 14;
                double olhoDireitoY = y + 4;

                // Ajusta a posição dos olhos para olhar para a direção certa
                switch (direcaoAtual) {
                    case CIMA:
                        olhoEsquerdoY = y + 3;
                        olhoDireitoY = y + 3;
                        break;
                    case BAIXO:
                        olhoEsquerdoY = y + 15;
                        olhoDireitoY = y + 15;
                        break;
                    case ESQUERDA:
                        olhoEsquerdoX = x + 3;
                        olhoDireitoX = x + 3;
                        olhoEsquerdoY = y + 4;
                        olhoDireitoY = y + 14;
                        break;
                    case DIREITA:
                        olhoEsquerdoX = x + 15;
                        olhoDireitoX = x + 15;
                        olhoEsquerdoY = y + 4;
                        olhoDireitoY = y + 14;
                        break;
                }

                // Desenha os globos oculares brancos
                gc.fillOval(olhoEsquerdoX, olhoEsquerdoY, olhoTamanho, olhoTamanho);
                gc.fillOval(olhoDireitoX, olhoDireitoY, olhoTamanho, olhoTamanho);

                // Desenha as pupilas pretas olhando na direção do movimento
                gc.setFill(Color.BLACK);
                double pupilaTamanho = 3;
                double pOffset = 2; // Deslocamento da pupila para simular olhar direcionado
                
                double pEx = olhoEsquerdoX + 2;
                double pEy = olhoEsquerdoY + 2;
                double pDx = olhoDireitoX + 2;
                double pDy = olhoDireitoY + 2;

                switch (direcaoAtual) {
                    case CIMA:    pEy -= 1; pDy -= 1; break;
                    case BAIXO:   pEy += 1; pDy += 1; break;
                    case ESQUERDA:pEx -= 1; pDx -= 1; break;
                    case DIREITA: pEx += 1; pDx += 1; break;
                }

                gc.fillOval(pEx, pEy, pupilaTamanho, pupilaTamanho);
                gc.fillOval(pDx, pDy, pupilaTamanho, pupilaTamanho);

            } else {
                // CORPO DA COBRA (Segmentos redondos de desenho com efeito de luz)
                gc.setFill(Color.web("#2ecc71")); // Verde claro cartoon
                gc.fillOval(x + 1, y + 1, TAMANHO_BLOCO - 2, TAMANHO_BLOCO - 2);

                // Brilho 2D interno (Cria o efeito de volume arredondado de desenho)
                gc.setFill(Color.web("#a3e4d7", 0.5)); // Verde-claro transparente
                gc.fillOval(x + 4, y + 4, 6, 6);
            }
        }

        // HUD
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial Bold", 18));
        gc.fillText("Pontos: " + pontuacao, 30, 40);
        gc.fillText("Dificuldade: " + dificuldadeAtual, larguraPixels - 220, 40);

        // Fim de Jogo
        if (gameOver) {
            gc.setFill(Color.color(0.9, 0.1, 0.1, 0.85));
            gc.fillRect(0, 0, larguraPixels, alturaPixels);

            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial Bold", 48));
            gc.fillText("GAME OVER", larguraPixels / 2.0 - 145, alturaPixels / 2.0 - 50);
            
            gc.setFont(new Font("Arial", 24));
            gc.fillText("Pontuação Final: " + pontuacao, larguraPixels / 2.0 - 105, alturaPixels / 2.0 + 20);
            
            gc.setFont(new Font("Arial Bold", 18));
            gc.fillText("[ R ] - Recomeçar", larguraPixels / 2.0 - 150, alturaPixels / 2.0 + 100);
            gc.fillText("[ M ] - Menu Principal", larguraPixels / 2.0 - 150, alturaPixels / 2.0 + 140);
            gc.fillText("[ ESC ] - Sair do Jogo", larguraPixels / 2.0 - 150, alturaPixels / 2.0 + 180);
        }
    }

    private void gerarComida() {
        boolean emCimaDaCobra;
        do {
            comida[0] = random.nextInt(larguraTabuleiro);
            comida[1] = random.nextInt(alturaTabuleiro);

            emCimaDaCobra = false;
            for (int[] parte : cobra) {
                if (parte[0] == comida[0] && parte[1] == comida[1]) {
                    emCimaDaCobra = true;
                    break;
                }
            }
        } while (emCimaDaCobra);
    }

    private void reiniciarJogo() {
        cobra.clear();
        int spawnX = (larguraTabuleiro > 0) ? larguraTabuleiro / 2 : 10;
        int spawnY = (alturaTabuleiro > 0) ? alturaTabuleiro / 2 : 10;

        cobra.add(new int[]{spawnX, spawnY});
        cobra.add(new int[]{spawnX - 1, spawnY});
        cobra.add(new int[]{spawnX - 2, spawnY});
        direcaoAtual = Direcao.DIREITA;
        pontuacao = 0;
        gameOver = false;
        gerarComida();
    }

    public static void main(String[] args) {
        Application.launch(Principal.class, args);
    }
}