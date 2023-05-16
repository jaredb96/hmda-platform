package hmda.validation.engine

// $COVERAGE-OFF$
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.quality._2019._
import hmda.validation.rules.lar.quality.{_2020 => quality2020, _2021 => quality2021, _2022 => quality2022}
import hmda.validation.rules.lar.quality.common._
import hmda.validation.rules.lar.syntactical.{S300, S301}
import hmda.validation.rules.lar.validity.{_2020, _2022, _}
import hmda.census.records.CensusRecords

private[engine] object LarEngine2023Q extends ValidationEngine[LoanApplicationRegister] {

  override def syntacticalChecks(ctx: ValidationContext) = Vector(
    S300,
    S301.withContext(ctx)
  )

  override def validityChecks(ctx: ValidationContext) = Vector(
    V600,
    V608_1,
    _2022.V608_2,
    V609,
    V610_1,
    V610_2,
    V611,
    V612_1,
    V612_2,
    V613_1,
    V613_2,
    V613_3,
    V613_4,
    V614_1,
    V614_2,
    V614_3,
    V614_4,
    V615_1,
    V615_2,
    V615_3,
    V616,
    V617,
    V618,
    V619_1,
    _2023.V619_2,
    V619_3,
    V620,
    V621,
    V622,
    V623,
    V624,
    V625_1,
    V625_2.withIndexedTracts(CensusRecords.indexedTract2022),
    V626.withIndexedCounties(CensusRecords.indexedCounty2022),
    V628_1,
    V628_2,
    V628_3,
    V628_4,
    V629_1,
    V629_2,
    V629_3,
    V630,
    V631_1,
    V631_2,
    V631_3,
    V631_4,
    V632_1,
    V632_2,
    V632_3,
    V633,
    V634,
    V635_1,
    V635_2,
    V635_3,
    V635_4,
    V636_1,
    V636_2,
    V636_3,
    V637,
    V638_1,
    V638_2,
    V638_3,
    V638_4,
    V639_1,
    V639_2,
    V639_3,
    V640,
    V641,
    V642_1,
    V642_2,
    V643,
    V644_1,
    _2020.V644_2,
    V645,
    V646_1,
    V646_2,
    V647,
    V648_1,
    _2020.V648_2,
    V649,
    V650,
    V651_1,
    V651_2,
    V652_1,
    V652_2,
    V654_1,
    V654_2,
    V655_1,
    V655_2,
    V656_1,
    V656_2,
    V657_1,
    V657_2,
    V657_3,
    V658_1,
    V658_2,
    V659,
    V660_1,
    _2022.V660_2,
    V661,
    _2022.V662_1,
    _2022.V662_2,
    V663,
    V664,
    _2022.V665_1,
    _2022.V665_2,
    V666_1,
    V666_2,
    V667_1,
    V667_2,
    V668_1,
    V668_2,
    V669_1,
    V669_2,
    V669_3,
    V669_4,
    V670_1,
    V670_2,
    V670_3,
    V670_4,
    V671_1,
    V671_2,
    V672_1,
    V672_2,
    V672_3,
    V672_4,
    V672_5,
    V672_6,
    V673_1,
    V673_2,
    V673_3,
    V673_4,
    V673_5,
    V674_1,
    V674_2,
    V674_3,
    V674_4,
    V674_5,
    V675_1,
    V675_2,
    V675_3,
    V675_4,
    V675_5,
    V676_1,
    V676_2,
    V676_3,
    V676_4,
    V676_5,
    _2020.V677_1,
    V677_2,
    V678_1,
    V678_2,
    V678_3,
    V678_4,
    V678_5,
    V679_1,
    V679_2,
    V679_3,
    V680_1,
    V680_2,
    V681_1,
    V681_2,
    V682_1,
    V682_2,
    V683,
    V684,
    V685,
    V686,
    V687,
    V688_1,
    V688_2,
    V689_1,
    V689_2,
    V689_3,
    V690_1,
    V690_2,
    V690_3,
    V691,
    V692_1,
    V692_2,
    V692_3,
    V693_1,
    V693_2,
    V693_3,
    V694_1,
    V694_2,
    V694_3,
    _2021.V695_1,
    _2021.V695_2,
    _2021.V696_1,
    _2021.V696_2,
    V696_3,
    _2020.V699,
    V700_1,
    V700_2,
    V701,
    V702_1,
    V702_2,
    V703_1,
    V703_2,
    V704_1,
    V704_2,
    V705_1,
    V705_2,
    V706,
    V707,
    V708,
    V709,
    V710,
    V711,
    V712,
    V713,
    V714,
    V715,
    _2020.V716.withIndexedCounties(CensusRecords.indexedCounty2022),
    _2022.V721_1,
    _2022.V721_2,
    _2022.V720_1,
    _2022.V720_2
  )

  override val qualityChecks = Vector(
    Q601,
    Q602,
    Q603.withIndexedSmallCounties(CensusRecords.indexedSmallCounty2022),
    Q605_1,
    Q605_2,
    quality2020.Q606,
    Q609,
    Q607,
    Q608,
    Q610,
    Q611,
    Q612,
    Q613,
    quality2020.Q614_1,
    quality2020.Q614_2,
    quality2021.Q615_1,
    quality2021.Q615_2,
    Q616_1,
    Q616_2,
    quality2020.Q617,
    Q618,
    Q619,
    Q620,
    Q621,
    Q622,
    Q623,
    Q624,
    Q625,
    Q626,
    Q627,
    Q628,
    Q629,
    Q630,
    Q631,
    Q632,
    Q633,
    Q642_1,
    Q642_2,
    Q643,
    Q644,
    Q645_1,
    Q645_2,
    quality2020.Q648,
    quality2020.Q649_1,
    quality2020.Q649_2,
    quality2020.Q651,
    quality2020.Q652,
    quality2021.Q650_1,
    quality2021.Q650_2,
    quality2020.Q653_1,
    quality2020.Q653_2,
    quality2022.Q654,
    quality2021.Q655,
    quality2021.Q656,
    quality2021.Q657,
    quality2022.Q658,
    quality2022.Q659_1,
    quality2022.Q659_2
  )
}
// $COVERAGE-ON$
