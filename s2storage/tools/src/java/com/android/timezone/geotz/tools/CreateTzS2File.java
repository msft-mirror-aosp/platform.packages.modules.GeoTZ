/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.timezone.geotz.tools;

import com.android.timezone.geotz.storage.tzs2range.TzS2Range;
import com.android.timezone.geotz.storage.tzs2range.TzS2RangeFileFormat;
import com.android.timezone.geotz.storage.tzs2range.write.TzS2RangeFileWriter;
import com.android.timezone.geotz.tools.proto.GeotzProtos;
import com.google.protobuf.TextFormat;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

/** Creates a TZ S2 file from a text proto file. */
public final class CreateTzS2File {

    /*
     * Usage:
     * CreateTzS2File <[input] proto file> <[input] s2 level of input data> <[output] tz s2 file>
     *
     * The proto file is defined in geotz_protos.proto. The data must be ordered correctly.
     */
    public static void main(String[] args) throws Exception {
        File inputFile = new File(args[0]);
        int s2Level = Integer.parseInt(args[1]);
        File outputFile = new File(args[2]);

        GeotzProtos.TimeZones timeZonesInput;
        try (FileReader reader = new FileReader(inputFile)) {
            GeotzProtos.TimeZones.Builder builder = GeotzProtos.TimeZones.newBuilder();
            TextFormat.getParser().merge(reader, builder);
            timeZonesInput = builder.build();
        }

        TzS2RangeFileFormat fileFormat = FileFormats.getFileFormatForLevel(s2Level);
        try (TzS2RangeFileWriter writer = TzS2RangeFileWriter.open(outputFile, fileFormat)) {
            List<GeotzProtos.TimeZoneIdSet> timeZoneIdSets = timeZonesInput.getTimeZoneIdSetsList();
            Iterator<TzS2Range> tzS2RangeIterator = timeZonesInput.getRangesList()
                    .stream()
                    .map(x -> createTzS2Range(timeZoneIdSets, x))
                    .iterator();
            writer.processRanges(tzS2RangeIterator);
        }
    }

    private static TzS2Range createTzS2Range(List<GeotzProtos.TimeZoneIdSet> tzIdStrings,
            GeotzProtos.S2Range inputRange) {
        GeotzProtos.TimeZoneIdSet timeZoneIdSet =
                tzIdStrings.get(inputRange.getTimeZoneIdSetIndex());
        List<String> rangeStrings = timeZoneIdSet.getTimeZoneIdsList();
        return new TzS2Range(inputRange.getStartCellId(), inputRange.getEndCellId(), rangeStrings);
    }
}
