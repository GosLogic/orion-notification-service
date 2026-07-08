# language: es
# Pruebas BDD que garantizan la Trazabilidad de alertas críticas y el aislamiento
# multi-tenant en la comunicación del Notification Service (Orion SaaS).

Característica: Historial de Notificaciones por Tenant
  Como back-office de un tenant autenticado en Orion
  Quiero consultar únicamente el historial de notificaciones de mi organización
  Para garantizar el aislamiento multi-tenant en la comunicación de alertas

  Escenario: Consultar historial de notificaciones
    Dado un tenant autenticado
    Cuando solicita su historial
    Entonces el sistema devuelve la lista de notificaciones ordenadas de forma descendente

  Escenario: Restricción de acceso a notificaciones de otros tenants
    Dado un usuario del tenant A
    Cuando intenta consultar el detalle de una notificación del tenant B
    Entonces el sistema bloquea el acceso devolviendo un error 404
