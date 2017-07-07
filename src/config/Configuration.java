package config.Configuration;

import java.util.*;
import java.io.*;

/**
 * @author Tapio Sivonen
 *
 * Generic configuration class using java.util.Properties.
 *
 * Checks if itself or a list of chained Properties or System
 * contains the given property.
 *
 * Configuration is thread safe with synchronization.
 **/

public class Configuration
    extends Properties
{
    /**
     * HashMap to implement singletons.
     **/
    private static HashMap<String, Configuration> singletonConfigurations
        = new HashMap<String, Configuration>();

    /**
     * @return default singleton Configuration.
     * @throws IllegalStateException if an IOException occurs in constructing the Configuration
     **/
    public synchronized static Configuration singleton()
    {
        return singleton(DEFAULT_FILENAME);
    }

    /**
     * @param filename name of file to read the configuration from.
     * @return singleton Configuration found from filename.
     * @throws IllegalStateException if an IOException occurs in constructing the Configuration
     **/
    public synchronized static Configuration singleton(String filename)
    {
        try {
            return
                singletonConfigurations
                .computeIfAbsent(filename, () -> new Configuration(filename));
        }
        catch(IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * Default Configuration filename
     **/
    public static String DEFAULT_FILENAME
        = ".config/config";

    /**
     * Chained Properties.
     **/
    private List<Properties> chain = new ArrayList<Properties>();

    /**
     * Default constructor, does Configuration(DEFAULT_FILENAME)
     *
     * @return constructed Configuration
     * @throws IOException if an IOException occurs loading the file
     **/
    public Configuration()
        throws IOException
    {
        this(DEFAULT_FILENAME);
    }

    /**
     * Constructor that loads its Properties backer from filename
     *
     * @param filename name of the file to load Properties from
     * @return constructed Configuration
     * @throws IOException if an IOException occurs loading the file
     **/
    public Configuration(String filename)
        throws IOException
    {
        try (Reader fileReader
             = new FileReader(filename)) {          
            load(fileReader);
        }
    }

    /**
     * Adds target Properties for chain to check properties from in the order they appear.
     *
     * @param target some Properties to add to lookup chain.
     * @return this for chaining calls
     **/
    public synchronized Configuration addProperties(Properties... target)
    {
        chain.addAll(Arrays.asList(target));
        return this;
    }

    /**
     * Removes all occurances of each target Properties from lookup chain.
     *
     * @param target some Properties to remove from lookup chain.
     * @return this for chaining calls
     **/
    public synchronized Configuration removeProperties(Properties... target)
    {
        chain.removeAll(Arrays.asList(target));
        return this;
    }

    /**
     * Gets the key property value from this Configuration's properties
     * or the lookup chain Properties (in order of addition) or System
     * properties if security allows.
     *
     * @param key key to look up value for
     * @return corresponding value or null if not found
     **/
    @Override
    public synchronized String getProperty(String key)
    {
        return getProperty(key, null);
    }

    /**
     * Gets the key property value from this Configuration's properties
     * or the lookup chain Properties (in order of addition) or System
     * properties if security allows.
     *
     * @param key key to look up value for
     * @param defaultValue value to return in case no value is found
     * @return corresponding value or defaultValue if not found
     **/
    @Override
    public synchronized String getProperty(String key, String defaultValue)
    {
        String result = null;
        boolean found = false;
        if(super.containsKey(key)) {
            result = super.get(key);
            found = true;
        }
        if(!found) {
            for(Properties p : chain) {
                if(p.containsKey(key)) {
                    result = p.get(key);
                    found = true;
                    break;
                }
            }
        }
        if(!found) {
            try {
                String sysResult = System.getProperty(key);
                if(null != sysResult) {
                    result = sysResult;
                    found = true;
                }
            }
            catch(SecurityException se) {
            }
        }
        if(found)
            return result;
        return defaultValue;
    }
}
