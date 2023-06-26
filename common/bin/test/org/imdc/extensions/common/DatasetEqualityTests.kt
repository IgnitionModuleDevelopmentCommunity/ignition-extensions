package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.BasicDataset
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.imdc.extensions.common.DSBuilder.Companion.dataset
import java.util.Date

class DatasetEqualityTests : FunSpec(
    {
        val dataset1 = dataset {
            column("a", listOf(1, 2, 3))
            column("b", listOf(3.14, 2.18, 4.96))
            column("c", listOf("string", "strung", "strang"))
        }
        val copyOfDs1 = BasicDataset(dataset1)
        val ds1WithCaps = BasicDataset(listOf("A", "B", "C"), dataset1.columnTypes, dataset1)
        val ds1WithOtherNames = BasicDataset(listOf("ant", "bat", "cat"), dataset1.columnTypes, dataset1)
        val dataset2 = dataset {
            column("j", listOf(3, 2, 1))
            column("k", listOf(1.0, 2.0, 3.0))
            column("l", listOf("chess", "chass", "chuss"))
        }
        val copyOfDs2 = BasicDataset(dataset2)
        val ds2WithCaps = BasicDataset(listOf("J", "K", "L"), dataset2.columnTypes, dataset2)
        val ds2WithOtherNames = BasicDataset(listOf("joe", "kim", "lee"), dataset2.columnTypes, dataset2)

        val dataset3 = dataset {
            column("t_stamp", listOf(1667605391000, 1667605392000, 1667605393000, 1667605394000).map(::Date))
            column("tag_1", listOf(1.0, 2.0, 3.0, 4.0))
            column("tag_2", listOf(true, false, true, false))
        }
        val copyOfDs3 = BasicDataset(dataset3)
        val ds3WithAliases = BasicDataset(listOf("timestamp", "doubleTag", "boolTag"), dataset3.columnTypes, dataset3)

        context("General equality") {
            test("Same dataset") {
                DatasetExtensions.equals(dataset1, dataset1) shouldBe true
                DatasetExtensions.equals(dataset2, dataset2) shouldBe true
                DatasetExtensions.equals(dataset3, dataset3) shouldBe true
            }

            test("Copied dataset") {
                DatasetExtensions.equals(dataset1, copyOfDs1) shouldBe true
                DatasetExtensions.equals(dataset2, copyOfDs2) shouldBe true
                DatasetExtensions.equals(dataset3, copyOfDs3) shouldBe true
            }

            test("Different datasets") {
                DatasetExtensions.equals(dataset1, dataset2) shouldBe false
                DatasetExtensions.equals(dataset1, dataset3) shouldBe false
                DatasetExtensions.equals(dataset2, dataset3) shouldBe false
            }

            test("Datasets with same structure, but different columns") {
                DatasetExtensions.equals(dataset1, ds1WithCaps) shouldBe false
                DatasetExtensions.equals(dataset1, ds1WithOtherNames) shouldBe false
                DatasetExtensions.equals(dataset2, ds2WithCaps) shouldBe false
                DatasetExtensions.equals(dataset2, ds2WithOtherNames) shouldBe false
                DatasetExtensions.equals(dataset3, ds3WithAliases) shouldBe false
            }
        }

        context("Value equality") {
            test("Same dataset") {
                DatasetExtensions.valuesEqual(dataset1, dataset1) shouldBe true
                DatasetExtensions.valuesEqual(dataset2, dataset2) shouldBe true
                DatasetExtensions.valuesEqual(dataset3, dataset3) shouldBe true
            }

            test("Copied dataset") {
                DatasetExtensions.valuesEqual(dataset1, copyOfDs1) shouldBe true
                DatasetExtensions.valuesEqual(dataset2, copyOfDs2) shouldBe true
                DatasetExtensions.valuesEqual(dataset3, copyOfDs3) shouldBe true
            }

            test("Different datasets") {
                DatasetExtensions.valuesEqual(dataset1, dataset2) shouldBe false
                DatasetExtensions.valuesEqual(dataset1, dataset3) shouldBe false
                DatasetExtensions.valuesEqual(dataset2, dataset3) shouldBe false
            }

            test("Datasets with same structure, but different columns") {
                DatasetExtensions.valuesEqual(dataset1, ds1WithCaps) shouldBe true
                DatasetExtensions.valuesEqual(dataset1, ds1WithOtherNames) shouldBe true
                DatasetExtensions.valuesEqual(dataset2, ds2WithCaps) shouldBe true
                DatasetExtensions.valuesEqual(dataset2, ds2WithOtherNames) shouldBe true
                DatasetExtensions.valuesEqual(dataset3, ds3WithAliases) shouldBe true
            }
        }

        context("Column equality") {
            test("Same dataset") {
                DatasetExtensions.columnsEqual(dataset1, dataset1) shouldBe true
                DatasetExtensions.columnsEqual(dataset2, dataset2) shouldBe true
                DatasetExtensions.columnsEqual(dataset3, dataset3) shouldBe true
            }

            test("Copied dataset") {
                DatasetExtensions.columnsEqual(dataset1, copyOfDs1) shouldBe true
                DatasetExtensions.columnsEqual(dataset2, copyOfDs2) shouldBe true
                DatasetExtensions.columnsEqual(dataset3, copyOfDs3) shouldBe true
            }

            test("Different datasets") {
                DatasetExtensions.columnsEqual(dataset1, dataset2) shouldBe false
                DatasetExtensions.columnsEqual(dataset1, dataset3) shouldBe false
                DatasetExtensions.columnsEqual(dataset2, dataset3) shouldBe false
            }

            test("Datasets with same structure, but different columns") {
                DatasetExtensions.columnsEqual(dataset1, ds1WithCaps) shouldBe false
                DatasetExtensions.columnsEqual(dataset1, ds1WithCaps, ignoreCase = true) shouldBe true
                DatasetExtensions.columnsEqual(dataset1, ds1WithOtherNames) shouldBe false
                DatasetExtensions.columnsEqual(dataset2, ds2WithCaps) shouldBe false
                DatasetExtensions.columnsEqual(dataset2, ds2WithCaps, ignoreCase = true) shouldBe true
                DatasetExtensions.columnsEqual(dataset2, ds2WithOtherNames) shouldBe false
                DatasetExtensions.columnsEqual(dataset3, ds3WithAliases) shouldBe false

                val ds1WithDifferentTypes = BasicDataset(
                    dataset1.columnNames,
                    listOf(String::class.java, Int::class.java, Boolean::class.java),
                    dataset1,
                )
                DatasetExtensions.columnsEqual(dataset1, ds1WithDifferentTypes) shouldBe false
                DatasetExtensions.columnsEqual(dataset1, ds1WithDifferentTypes, ignoreCase = true) shouldBe false
                DatasetExtensions.columnsEqual(dataset1, ds1WithDifferentTypes, includeTypes = false) shouldBe true
                DatasetExtensions.columnsEqual(
                    dataset1,
                    ds1WithDifferentTypes,
                    includeTypes = false,
                    ignoreCase = true,
                ) shouldBe true

                val ds1WithDifferentTypesAndCase = BasicDataset(
                    listOf("A", "B", "C"),
                    dataset1.columnTypes,
                    dataset1,
                )
                DatasetExtensions.columnsEqual(dataset1, ds1WithDifferentTypesAndCase) shouldBe false
                DatasetExtensions.columnsEqual(dataset1, ds1WithDifferentTypesAndCase, ignoreCase = true) shouldBe true
                DatasetExtensions.columnsEqual(
                    dataset1,
                    ds1WithDifferentTypesAndCase,
                    includeTypes = false,
                ) shouldBe false
                DatasetExtensions.columnsEqual(
                    dataset1,
                    ds1WithDifferentTypesAndCase,
                    ignoreCase = true,
                    includeTypes = false,
                ) shouldBe true
            }
        }
    },
)
