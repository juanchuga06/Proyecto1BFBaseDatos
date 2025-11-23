public class ItemDoctor {
    private int id;
    private String nombre;

    public ItemDoctor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    // Este método es MÁGICO: El ComboBox lo usa para saber qué texto mostrar
    @Override
    public String toString() {
        return nombre;
    }
}