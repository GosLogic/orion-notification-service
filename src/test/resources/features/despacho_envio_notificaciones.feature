# language: es
# Pruebas BDD que garantizan la Trazabilidad de alertas críticas y el aislamiento
# multi-tenant en la comunicación del Notification Service (Orion SaaS).

Característica: Despacho y Envío de Notificaciones
  Como microservicio transversal de alertas de Orion
  Quiero despachar notificaciones con contenido explícito o generado por plantillas
  Para asegurar la trazabilidad de alertas críticas hacia conductores y gestores de flota

  Escenario: Despachar notificación con contenido completo
    Dado una solicitud de alarma con título y mensaje
    Cuando el sistema procesa el despacho
    Entonces se persiste la notificación y se envía el correo electrónico al conductor

  Escenario: Uso de plantillas para notificaciones sin contenido
    Dado una solicitud sin título ni mensaje
    Cuando el servicio la procesa
    Entonces utiliza el TemplateManager para generar el contenido dinámico correspondiente al tipo de alerta

  Escenario: Normalización del tipo de notificación
    Dado un tipo de notificación enviado en minúsculas como "alarm"
    Cuando el sistema lo interpreta
    Entonces normaliza el casing correctamente al enum esperado
