package hmda.publisher.validation

import slick.lifted.{ AbstractTable, Rep, TableQuery }

// abstraction for transmittal sheet tables to use in validation checks. Abstracts over years
trait TsData {
  type Entry
  type Table <: AbstractTable[Entry]
  val query: TableQuery[Table]
  val getLei: Table => Rep[String]
  val getTotalLines: Table => Rep[Int]
  val getSubmissionId: Table => Rep[Option[String]]
}
object TsData {
  def apply[Entry_, Table_ <: AbstractTable[Entry_]](
                                                      query_ : TableQuery[Table_]
                                                    )(getLei_ : Table_ => Rep[String], getTotalLines_ : Table_ => Rep[Int], getSubmissionId_ : Table_ => Rep[Option[String]]): TsData =
    new TsData {
      override type Entry = Entry_
      override type Table = Table_
      override val query: TableQuery[Table]                      = query_
      override val getLei: Table_ => Rep[String]                 = getLei_
      override val getTotalLines: Table_ => Rep[Int]             = getTotalLines_
      override val getSubmissionId: Table => Rep[Option[String]] = getSubmissionId_
    }
}

// abstraction for lar tables to use in validation checks. Abstracts over years and modified/non-modifed lars
trait LarData {
  type Entry
  type Table <: AbstractTable[Entry]
  val query: TableQuery[Table]
  val getLei: Table => Rep[String]
}

object LarData {
  def apply[Entry_, Table_ <: AbstractTable[Entry_]](
                                                      query_ : TableQuery[Table_]
                                                    )(getLei_ : Table_ => Rep[String]): LarData =
    new LarData {
      override type Entry = Entry_
      override type Table = Table_
      override val query: TableQuery[Table]      = query_
      override val getLei: Table_ => Rep[String] = getLei_
    }
}