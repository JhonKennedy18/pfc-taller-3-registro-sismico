package taller

/**
 * Taller 3 — consultas sobre un registro sísmico con expresiones for, mapas y
 * secuencias perezosas.
 *
 * Complete cada función. Las firmas y los tipos no se modifican.
 */
class RegistroSismico {

  type Registro = List[Sismo]

  // Punto 1: consultas con expresiones for --------------------------------------

  /** Las regiones de los sismos que superan el umbral, en orden y con repetidos. */
  def fuertes(r: Registro, umbral: Double): List[String] =
    for {
      s <- r
      if s.magnitud > umbral
    } yield s.region

  /** Las magnitudes registradas en una región. */
  def enRegion(r: Registro, region: String): List[Double] =
    for {
      s <- r
      if s.region == region
    } yield s.magnitud

  /** Parejas de regiones distintas con algún sismo de magnitudes cercanas. */
  def paresCercanos(r: Registro, dif: Double): List[(String, String)] = {
    val regiones = (for (s <- r) yield s.region).distinct

    val tolerancia = 1e-9

    def cercanas(a: String, b: String): Boolean =
      (for {
        s1 <- r
        s2 <- r
        if s1.region == a && s2.region == b
        if Math.abs(s1.magnitud - s2.magnitud) <= dif + tolerancia
      } yield (s1, s2)).nonEmpty

    for {
      a <- regiones
      b <- regiones
      if regiones.indexOf(a) < regiones.indexOf(b)
      if cercanas(a, b)
    } yield (a, b)
  }

  // Punto 2: agrupar con mapas --------------------------------------------------

  def porRegion(r: Registro): Map[String, List[Sismo]] =
    r.groupBy(_.region)

  def conteoPorRegion(r: Registro): Map[String, Int] =
    porRegion(r).map { case (region, sismos) => (region, sismos.length) }

  def magnitudMaxima(r: Registro): Map[String, Double] =
    porRegion(r).map { case (region, sismos) => (region, sismos.map(_.magnitud).max) }

  // Punto 3: consultas seguras --------------------------------------------------

  /** Un mapa que responde cero por las regiones que no están en el registro. */
  def conteoSeguro(r: Registro): Map[String, Int] =
    conteoPorRegion(r).withDefaultValue(0)

  /** La magnitud máxima de una región, o vacío si no hay registros suyos. */
  def maximaDe(r: Registro, region: String): Option[Double] =
    magnitudMaxima(r).get(region)

  /** La región con más sismos; en empate, la primera en orden alfabético. */
  def regionMasActiva(r: Registro): Option[String] =
    conteoPorRegion(r).toList
      .sortBy {case (region , n) => (-n, region) }
      .headOption
      .map(_._1)

  // Punto 4: réplicas perezosas -------------------------------------------------

  def replicas(magnitud: Double): LazyList[Double] = {
    def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0

    def desde(actual: Double): LazyList[Double] =
      redondear(actual) #:: desde(actual * 0.8)

    desde(magnitud)

  }

  /** Las réplicas mientras superen el mínimo, incluida la primera. */
  def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
    replicas(magnitud).takeWhile(_ > minimo).toList


  // Punto 5: informe combinado --------------------------------------------------

  /** Conteo, magnitud máxima y réplicas esperadas del sismo más fuerte, por región. */
  def informe(r: Registro, minimo: Double): Map[String, Resumen] = {
    val maximas = magnitudMaxima(r)

    conteoPorRegion(r).map { case (region, n) =>
      val maxima = maximas(region)
      (region, Resumen(n, maxima, replicasSensibles(maxima, minimo).length))
    }
  }
}