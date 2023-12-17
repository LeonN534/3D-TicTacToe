package vrtictactoe;

public class VRTicTacToe {
    
    public static String ganador;
    
    public static void main(String[] args) {
        Menu ventanaPrincipal = new Menu();
        ventanaPrincipal.setLocationRelativeTo(null);
        ventanaPrincipal.setTitle("TicTacToe");
        ventanaPrincipal.setResizable(false);
        ventanaPrincipal.setVisible(true);
    }
}
