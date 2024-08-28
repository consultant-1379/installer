/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.distocraft.dc5000.install.ant;

import java.sql.SQLException;

import ssc.rockfactory.RockFactory;

public class AddAggFlagToOldTechpackAntTask extends CommonDBTasks {

    private static final String COMMA = ",";

    private static final String ALL = "ALL";

    private String techpackName;

    private String confDirectory;

    private transient RockFactory dwhrepRockFactory = null;

    private transient RockFactory etlrepRockFactory = null;

    private transient RockFactory dwhRockFactory = null;

    @Override
    public void execute() {
        try {
            this.etlrepRockFactory = createEtlrepRockFactory();
            this.dwhrepRockFactory = createDbRockFactory(this.etlrepRockFactory, DWHREP);
            this.dwhRockFactory = createDbRockFactory(this.etlrepRockFactory, DWH);

            if (techpackName.equalsIgnoreCase(ALL)) {
                String techpackNames = AddAggFlagToOldTechpack.getListOfTpsWithAggFlag(confDirectory);
                String[] techpacks = techpackNames.split(COMMA);
                for (String techpack : techpacks) {
                    runAddAggFlagToOldTechpack(techpack);
                }
            } else {
                runAddAggFlagToOldTechpack(this.techpackName);
            }
        } finally {
            if (dwhRockFactory != null) {
                try {
                    dwhRockFactory.getConnection().close();
                } catch (SQLException e) {
                    System.out.println("etlrepRockFactory cleanup error ");
                }
            }

            if (dwhrepRockFactory != null) {
                try {
                    dwhrepRockFactory.getConnection().close();
                } catch (SQLException e) {
                    System.out.println("dwhrepRockFactory cleanup error ");
                }
            }

            if (etlrepRockFactory != null) {
                try {
                    etlrepRockFactory.getConnection().close();
                } catch (SQLException e) {
                    System.out.println("etlrepRockFactory cleanup error ");
                }
            }
        }
    }

    private void runAddAggFlagToOldTechpack(final String techpack) {
        final AddAggFlagToOldTechpack addAggFlagToOldTechpack = new AddAggFlagToOldTechpack(techpack, this.dwhrepRockFactory, this.dwhRockFactory,
                this.confDirectory);
        addAggFlagToOldTechpack.execute();
    }

    public String getTechpackName() {
        return techpackName;
    }

    public void setTechpackName(final String techpackName) {
        this.techpackName = techpackName;
    }

    public void setConfDirectory(final String dir) {
        confDirectory = dir;
    }

    public String getConfDirectory() {
        return confDirectory;
    }
}