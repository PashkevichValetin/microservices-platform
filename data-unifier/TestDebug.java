public class TestDebug {
    public static void main(String[] args) {
        System.out.println("=== DEBUG TEST ===");
        System.out.println("Если вы это видите, код компилируется");
        
        // Проверяем, что классы существуют
        try {
            Class.forName("com.pashcevich.data_unifier.service.DataUnificationService");
            System.out.println("DataUnificationService найден");
        } catch (Exception e) {
            System.out.println("DataUnificationService НЕ найден");
        }
    }
}
