const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

module.exports = {
  title: 'Overcast',
  tagline: 'Cloud test -- Java helper classes to write your tests against hosts in the cloud',
  url: 'https://github.com/xebialabs/overcast',
  baseUrl: '/overcast/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/digital_ai_deploy.svg',
  organizationName: 'Digital.ai',
  projectName: 'overcast',
  themeConfig: {
    navbar: {
      title: 'Overcast',
      logo: {
        alt: 'Overcast Digital.ai',
        src: 'img/digital_ai_deploy.svg',
      },
      items: [
        {
          type: 'doc',
          docId: 'intro',
          position: 'left',
          label: 'Tutorial',
        },

        {
          href: 'https://github.com/xebialabs/overcast',
          label: 'GitHub',
          position: 'right',
        }
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Tutorial',
              to: '/docs/intro',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/xebialabs/overcast',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Overcast Digital.ai`,
    },
    prism: {
      theme: lightCodeTheme,
      darkTheme: darkCodeTheme,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js')
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
