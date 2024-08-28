package com.distocraft.dc5000.install.ant;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;

import ssc.rockfactory.RockFactory;

public class PartitionRolloverToNextAntTask extends CommonDBTasks {

    private static final String COMMA = ",";

    private static final String ALL = "ALL";

    private String techpackName;

    private String confDirectory;

    private transient RockFactory dwhrepRockFactory = null;

    private transient RockFactory etlrepRockFactory = null;

    private transient RockFactory dwhRockFactory = null;

    @Override
    public void execute() throws BuildException {
        try {
            this.etlrepRockFactory = createEtlrepRockFactory();
            this.dwhrepRockFactory = createDbRockFactory(this.etlrepRockFactory, DWHREP);
            this.dwhRockFactory = createDbRockFactory(this.etlrepRockFactory, DWH);

            if (techpackName.equalsIgnoreCase(ALL)) {
                String techpackNames = PartitionRolloverToOldTechpack.getListOfTps(confDirectory);
                String[] techpacks = techpackNames.split(COMMA);
                for (String techpack : techpacks) {
                    runPartitionRollover(techpack);
                }
            } else {
            	runPartitionRollover(this.techpackName);
            }
        } catch (ParseErrorException e) {
			e.printStackTrace();
		} catch (MethodInvocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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

    private void runPartitionRollover(final String techpack) throws Exception {
        final PartitionRolloverToOldTechpack PartitionRolloverToOldTechpack = new PartitionRolloverToOldTechpack(techpack, this.dwhrepRockFactory, this.dwhRockFactory,
                this.confDirectory);
        PartitionRolloverToOldTechpack.execute();
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
