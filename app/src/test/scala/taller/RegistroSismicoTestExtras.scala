package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Datos.registro

/**
 * Casos de prueba propios del equipo: cinco o más por punto, distintos de los
 * ejemplos del enunciado y de los del archivo RegistroSismicoTest.
 *
 * Los valores esperados se calcularon con un programa independiente (fracciones
 * exactas, sin reutilizar el código de RegistroSismico).
 */
@RunWith(classOf[JUnitRunner])
class RegistroSismicoTestExtras extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  val vacio: List[Sismo] = List()

  // Otro registro, con una región repetida, un empate de magnitud entre regiones
  // y una región con un solo sismo.
  val otro: List[Sismo] = List(
    Sismo(Fecha(2024, 1), "Cauca", 4.5, 10),
    Sismo(Fecha(2024, 2), "Narino", 4.5, 20),
    Sismo(Fecha(2024, 2), "Cauca", 6.8, 15),
    Sismo(Fecha(2024, 3), "Huila", 2.9, 5),
    Sismo(Fecha(2024, 4), "Narino", 5.5, 30),
    Sismo(Fecha(2024, 4), "Cauca", 4.5, 12))

  def sismo(region: String, magnitud: Double): Sismo =
    Sismo(Fecha(2024, 1), region, magnitud, 10)

  // Punto 1 -------------------------------------------------------------------

  test("fuertes: umbral 4.5 es estricto, las magnitudes de 4.5 no salen") {
    assert(fuertes(otro, 4.5) == List("Cauca", "Narino"))
  }

  test("fuertes: con umbral 0.0 salen todos, con repetidos y en orden") {
    assert(fuertes(otro, 0.0) ==
      List("Cauca", "Narino", "Cauca", "Huila", "Narino", "Cauca"))
  }

  test("fuertes: el registro vacío no tiene sismos fuertes") {
    assert(fuertes(vacio, 1.0) == List())
  }

  test("fuertes: bajar el umbral nunca reduce el resultado") {
    val umbrales = List(7.0, 6.0, 5.0, 4.5, 3.0, 0.0)
    val largos = umbrales.map(u => fuertes(otro, u).length)
    assert(largos == List(0, 1, 2, 2, 5, 6))
    assert(largos.zip(largos.tail).forall { case (a, b) => a <= b })
  }

  test("enRegion: conserva los repetidos y el orden del registro") {
    assert(enRegion(otro, "Cauca") == List(4.5, 6.8, 4.5))
  }

  test("enRegion: una región con un solo sismo") {
    assert(enRegion(otro, "Huila") == List(2.9))
  }

  test("enRegion: distingue mayúsculas de minúsculas") {
    assert(enRegion(otro, "cauca") == List())
  }

  test("paresCercanos: con diferencia 0.0 solo se emparejan magnitudes idénticas") {
    assert(paresCercanos(otro, 0.0) == List(("Cauca", "Narino")))
  }

  test("paresCercanos: varios sismos coincidentes producen una sola pareja") {
    // Cauca tiene dos sismos de 4.5 y Narino uno: hay dos coincidencias, una pareja.
    assert(paresCercanos(otro, 1.0) == List(("Cauca", "Narino")))
  }

  test("paresCercanos: con 1.6 aparecen las tres parejas, en orden de aparición") {
    assert(paresCercanos(otro, 1.6) ==
      List(("Cauca", "Narino"), ("Cauca", "Huila"), ("Narino", "Huila")))
  }

  test("paresCercanos: con 1.5 Huila queda aislada") {
    assert(paresCercanos(otro, 1.5) == List(("Cauca", "Narino")))
  }

  test("paresCercanos: la diferencia es 'a lo sumo' aunque el Double la desvíe") {
    // 3.1 - 3.0 da 0.10000000000000009 en Double, pero la diferencia decimal es 0.1.
    val r = List(sismo("A", 3.0), sismo("B", 3.1))
    assert(paresCercanos(r, 0.1) == List(("A", "B")))
  }

  test("paresCercanos: el orden de las regiones es el de aparición, no el alfabético") {
    val r = List(sismo("Zulia", 4.0), sismo("Arauca", 4.2))
    assert(paresCercanos(r, 0.5) == List(("Zulia", "Arauca")))
  }

  test("paresCercanos: una sola región, aunque tenga muchos sismos, no da parejas") {
    val r = List(sismo("Cauca", 4.0), sismo("Cauca", 4.0), sismo("Cauca", 4.1))
    assert(paresCercanos(r, 10.0) == List())
  }

  test("paresCercanos: el registro vacío no da parejas") {
    assert(paresCercanos(vacio, 1.0) == List())
  }

  // Punto 2 -------------------------------------------------------------------

  test("porRegion: cada grupo conserva el orden del registro") {
    assert(porRegion(otro)("Cauca").map(_.fecha.mes) == List(1, 2, 4))
    assert(porRegion(otro)("Cauca").map(_.magnitud) == List(4.5, 6.8, 4.5))
  }

  test("porRegion: las llaves son exactamente las regiones del registro") {
    assert(porRegion(otro).keySet == Set("Cauca", "Narino", "Huila"))
  }

  test("porRegion: el registro vacío da el mapa vacío") {
    assert(porRegion(vacio) == Map())
  }

  test("conteoPorRegion: tres, dos y uno") {
    assert(conteoPorRegion(otro) == Map("Cauca" -> 3, "Narino" -> 2, "Huila" -> 1))
  }

  test("conteoPorRegion: duplicar el registro duplica cada conteo") {
    assert(conteoPorRegion(registro ++ registro) ==
      Map("Valle" -> 6, "Santander" -> 4, "Choco" -> 4))
  }

  test("conteoPorRegion: la suma de los conteos es el largo del registro") {
    assert(conteoPorRegion(otro).values.sum == otro.length)
    assert(conteoPorRegion(registro).values.sum == registro.length)
  }

  test("magnitudMaxima: la mayor de cada región en otro registro") {
    assert(magnitudMaxima(otro) ==
      Map("Cauca" -> 6.8, "Narino" -> 5.5, "Huila" -> 2.9))
  }

  test("magnitudMaxima: acepta magnitudes negativas") {
    val micro = List(sismo("X", -1.2), sismo("X", -0.5), sismo("X", -3.0))
    assert(magnitudMaxima(micro) == Map("X" -> -0.5))
  }

  test("magnitudMaxima: tiene las mismas regiones que conteoPorRegion") {
    assert(magnitudMaxima(otro).keySet == conteoPorRegion(otro).keySet)
  }

  test("conteoPorRegion y magnitudMaxima no dependen del orden del registro") {
    assert(conteoPorRegion(otro.reverse) == conteoPorRegion(otro))
    assert(magnitudMaxima(otro.reverse) == magnitudMaxima(otro))
  }

  // Punto 3 -------------------------------------------------------------------

  test("conteoSeguro: una región con un solo sismo responde uno") {
    assert(conteoSeguro(otro)("Huila") == 1)
  }

  test("conteoSeguro: una región ausente responde cero sin lanzar excepción") {
    assert(conteoSeguro(otro)("Amazonas") == 0)
    assert(conteoSeguro(otro)("") == 0)
  }

  test("conteoSeguro: el valor por defecto no agrega llaves al mapa") {
    val m = conteoSeguro(otro)
    assert(m("Amazonas") == 0)
    assert(m.get("Amazonas") == None)
    assert(m.keySet == Set("Cauca", "Narino", "Huila"))
  }

  test("conteoSeguro: coincide con conteoPorRegion en las regiones presentes") {
    assert(conteoSeguro(otro) == conteoPorRegion(otro))
  }

  test("maximaDe: la máxima de Cauca es 6.8 y la de Huila 2.9") {
    assert(maximaDe(otro, "Cauca") == Some(6.8))
    assert(maximaDe(otro, "Huila") == Some(2.9))
  }

  test("maximaDe: distingue mayúsculas de minúsculas y la cadena vacía") {
    assert(maximaDe(otro, "cauca") == None)
    assert(maximaDe(otro, "") == None)
  }

  test("maximaDe: sobre el registro vacío da vacío") {
    assert(maximaDe(vacio, "Cauca") == None)
  }

  test("regionMasActiva: Cauca con tres sismos") {
    assert(regionMasActiva(otro) == Some("Cauca"))
  }

  test("regionMasActiva: un empate entre todas gana la primera alfabéticamente") {
    val r = List(sismo("Narino", 4.0), sismo("Cauca", 4.0), sismo("Huila", 4.0))
    assert(regionMasActiva(r) == Some("Cauca"))
  }

  test("regionMasActiva: el empate se rompe solo entre las regiones empatadas arriba") {
    // Huila y Narino tienen dos sismos; Arauca, uno: gana Huila, no Arauca.
    val r = List(
      sismo("Narino", 4.0), sismo("Arauca", 4.0), sismo("Huila", 4.0),
      sismo("Narino", 4.0), sismo("Huila", 4.0))
    assert(regionMasActiva(r) == Some("Huila"))
  }

  test("regionMasActiva: con una sola región, es esa") {
    assert(regionMasActiva(List(sismo("Choco", 3.0))) == Some("Choco"))
  }

  test("regionMasActiva: no depende del orden del registro, ni con empate") {
    val empate = List(sismo("Narino", 4.0), sismo("Cauca", 4.0), sismo("Huila", 4.0))
    assert(regionMasActiva(otro.reverse) == regionMasActiva(otro))
    assert(regionMasActiva(empate.reverse) == regionMasActiva(empate))
  }

  // Punto 4 -------------------------------------------------------------------

  test("replicas: las primeras cinco de un sismo de 10.0") {
    assert(replicas(10.0).take(5).toList == List(10.0, 8.0, 6.4, 5.12, 4.1))
  }

  test("replicas: el redondeo no se acumula, solo se aplica al emitir") {
    // Si se redondeara cada paso, la posición 6 sería 0.78 en lugar de 0.79.
    assert(replicas(3.0).take(8).toList ==
      List(3.0, 2.4, 1.92, 1.54, 1.23, 0.98, 0.79, 0.63))
  }

  test("replicas: de un sismo de 1.0 salen magnitudes menores a una unidad") {
    assert(replicas(1.0).take(6).toList == List(1.0, 0.8, 0.64, 0.51, 0.41, 0.33))
  }

  test("replicas: un sismo de magnitud cero da una secuencia de ceros") {
    assert(replicas(0.0).take(3).toList == List(0.0, 0.0, 0.0))
  }

  test("replicas: acceder por índice no cuelga y da el valor esperado") {
    assert(replicas(5.0)(10) == 0.54)
  }

  test("replicas: pedir cuatro elementos no calcula el quinto") {
    val l = replicas(5.0)
    assert(l.take(4).toList == List(5.0, 4.0, 3.2, 2.56))
    assert(l.toString.contains("<not computed>"))
  }

  test("replicas: nunca crece, cada réplica es menor o igual que la anterior") {
    val l = replicas(9.4).take(40).toList
    assert(l.zip(l.tail).forall { case (a, b) => b <= a })
  }

  test("replicasSensibles: con mínimo 5.0 desde 10.0 quedan cuatro") {
    assert(replicasSensibles(10.0, 5.0) == List(10.0, 8.0, 6.4, 5.12))
  }

  test("replicasSensibles: el mínimo es estricto también en el interior de la secuencia") {
    // 3.2 aparece en la secuencia, pero no supera el mínimo 3.2.
    assert(replicasSensibles(5.0, 3.2) == List(5.0, 4.0))
  }

  test("replicasSensibles: un mínimo apenas bajo la magnitud deja solo la primera") {
    assert(replicasSensibles(10.0, 9.99) == List(10.0))
  }

  test("replicasSensibles: con mínimo cero termina y devuelve 24 réplicas") {
    // Regresión: con el redondeo acumulado la secuencia se estancaba en 0.01 y esto se colgaba.
    val l = replicasSensibles(1.0, 0.0)
    assert(l.length == 24)
    assert(l.last == 0.01)
  }

  test("replicasSensibles: coincide con los primeros elementos de replicas") {
    assert(replicasSensibles(6.0, 1.0) ==
      List(6.0, 4.8, 3.84, 3.07, 2.46, 1.97, 1.57, 1.26, 1.01))
    assert(replicasSensibles(6.0, 1.0) ==
      replicas(6.0).take(replicasSensibles(6.0, 1.0).length).toList)
  }

  // Punto 5 -------------------------------------------------------------------

  test("informe: resumen de otro registro con mínimo 3.0") {
    assert(informe(otro, 3.0) ==
      Map("Cauca" -> Resumen(3, 6.8, 4),
        "Narino" -> Resumen(2, 5.5, 3),
        "Huila" -> Resumen(1, 2.9, 0)))
  }

  test("informe: con mínimo 0.5 las réplicas esperadas crecen") {
    assert(informe(otro, 0.5) ==
      Map("Cauca" -> Resumen(3, 6.8, 12),
        "Narino" -> Resumen(2, 5.5, 11),
        "Huila" -> Resumen(1, 2.9, 8)))
  }

  test("informe: con mínimo 5.0 sobre el registro del enunciado") {
    assert(informe(registro, 5.0) ==
      Map("Valle" -> Resumen(3, 5.3, 1),
        "Santander" -> Resumen(2, 5.1, 1),
        "Choco" -> Resumen(2, 6.0, 1)))
  }

  test("informe: con un mínimo inalcanzable todas las regiones tienen cero réplicas") {
    assert(informe(registro, 100.0) ==
      Map("Valle" -> Resumen(3, 5.3, 0),
        "Santander" -> Resumen(2, 5.1, 0),
        "Choco" -> Resumen(2, 6.0, 0)))
  }

  test("informe: una región con tres sismos iguales cuenta tres y una sola máxima") {
    val r = List(sismo("R", 4.0), sismo("R", 4.0), sismo("R", 4.0))
    assert(informe(r, 3.0) == Map("R" -> Resumen(3, 4.0, 2)))
  }

  test("informe: tiene exactamente las regiones del registro") {
    assert(informe(otro, 3.0).keySet == conteoPorRegion(otro).keySet)
    assert(informe(otro, 3.0).size == 3)
  }

  test("informe: la suma de los sismos es el largo del registro") {
    assert(informe(otro, 3.0).values.map(_.sismos).sum == otro.length)
  }

  test("informe: no depende del orden del registro") {
    assert(informe(otro.reverse, 3.0) == informe(otro, 3.0))
  }
}

